/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Application

import Schemas.{CrossoverToReporterSchema, EMASchema, EventUnpackSchema, LastPriceOutputSchema}
import Serializers.{KafkaDeserializer, KafkaSerializerQ1, KafkaSerializerQ2}
import Utilities.{ReportQ1, ReportQ2}
import com.twitter.chill.protobuf.ProtobufSerializer
import grpc.modules._
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.kafka.clients.producer.ProducerConfig

import java.util.Properties
import scala.util.Try

object StockMainApp {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val param = parameterParser(args)

    if (param._3 != -1.0) {
      println("Checkpoint Activate")
      env.enableCheckpointing(60000 * param._3)
      env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
      env.getCheckpointConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
      env.getCheckpointConfig.setCheckpointStorage("file://" + param._4)
    }

    /**
     * Register protobuf serializers/deserializers
     */
    env.getConfig.registerTypeWithKryoSerializer(classOf[Batch], classOf[ProtobufSerializer]) //Register our protobuf using this command
    env.getConfig.registerTypeWithKryoSerializer(classOf[ResultQ1], classOf[ProtobufSerializer])
    env.getConfig.registerTypeWithKryoSerializer(classOf[ResultQ2], classOf[ProtobufSerializer])
    env.setParallelism(2)


    /**
     * Kafka Producer Properties
     *  - TODO add in kafka config/server.properties
     *    transaction.max.timeout.ms=90000000
     */
    val kafkaProducerConfigProperties = new Properties()

    kafkaProducerConfigProperties.setProperty(
      ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "90000000"
    )
    kafkaProducerConfigProperties.setProperty(
      ProducerConfig.ACKS_CONFIG, "all"
    )
    kafkaProducerConfigProperties.setProperty(
      ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"
    )
    kafkaProducerConfigProperties.setProperty(
      ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1"
    )

    /**
     * Kafka Sinks for both Queries
     */
    val kafkaSinkQ1 = KafkaSink.builder()
      .setBootstrapServers("localhost:9092")
      .setKafkaProducerConfig(kafkaProducerConfigProperties)
      .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setTransactionalIdPrefix("Q1-Record-Producer")
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("topicQ1")
          .setValueSerializationSchema(KafkaSerializerQ1())
          .build()
      )
      .build()

    val kafkaSinkQ2 = KafkaSink.builder()
      .setBootstrapServers("localhost:9092")
      .setKafkaProducerConfig(kafkaProducerConfigProperties)
      .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setTransactionalIdPrefix("Q2-Record-Producer")
      .setRecordSerializer(
        KafkaRecordSerializationSchema.builder()
          .setTopic("topicQ2")
          .setValueSerializationSchema(KafkaSerializerQ2())
          .build()
      )
      .build()

    /**
     * Kafka Source for Data Ingest
     */
    val kafkaSource = KafkaSource.builder()
      .setBootstrapServers("localhost:9092")
      .setTopics("topic")
      .setGroupId("events-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(KafkaDeserializer())
      .build()


    /**
     * Ingest Data from Kafka Source
     */
    val sourceKafka: DataStream[Batch] = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source").name("Kafka Source")
    //    sourceKafka.print()


    val eventUnpacked: DataStream[EventUnpackSchema] = sourceKafka.flatMap(new BatchToEventUnpack()).name("BatchToEventUnpack")

    /**
     * This is the custom operator after data input
     */
    val processedWindow: DataStream[LastPriceOutputSchema] = eventUnpacked.keyBy(_.Symbol).process(new CustomLastPriceProcess()).setParallelism(3).name("Custom Window Processing")
    //    processedWindow.print()

    /**
     * Calculate the ema for each window
     */
    val emaWin: DataStream[EMASchema] = processedWindow.keyBy(_.Symbol).map(new EMACalculator()).name("EMA Calculator")
    //    emaWin.print()

    // Reporter for Q1
    val q1reporter: DataStream[ResultQ1] = emaWin.keyBy(_.batchID).process(new ReportQ1()).name("Q1Reporter").uid("Q1")
    q1reporter.sinkTo(kafkaSinkQ1).uid("sink1")
    //        q1reporter.print()

    /**
     * Calculate the crossover events
     */
    val crossoverCalc: DataStream[CrossoverToReporterSchema] = emaWin.keyBy(_.Symbol).process(new CrossoverCalculator)
    //    crossoverCalc.print()

    //     Reporter for Q2
    val q2reporter: DataStream[ResultQ2] = crossoverCalc.keyBy(_.batchID).process(new ReportQ2()).name("Q2Reporter").uid("Q2")
    q2reporter.sinkTo(kafkaSinkQ2).uid("sink2")
    //    q2reporter.print()



    env.execute("DEBS2022-Challenge")
  }

  def parameterParser(arguments: Array[String]): (Double, Double, Int, String) = {
    println(s"++++++Application Parameters++++++++")

    val chkLocation: String = arguments(3)

    val checkpointInterval: Int = if (Try(arguments(2).toInt).isSuccess) {
      arguments(2).toInt
    } else {
      -1
    }

    var j1: Double = 0.0
    var j2: Double = 0.0
    try {
      //j1 must be always smaller than the j2
      if (arguments(0).toDouble < arguments(1).toDouble) {
        j1 = arguments(0).toDouble
        j2 = arguments(1).toDouble
      } else {
        j1 = arguments(1).toDouble
        j2 = arguments(0).toDouble
      }

    } catch {
      case e: NumberFormatException => {
        //In case of an error give the default values
        j1 = 38
        j2 = 100
      }
    } finally {
      println(s"\n\tj1: ${j1}\n\tj2: ${j2}\n\tCheckpoint: ${
        if (checkpointInterval == -1.0) {
          "No Checkpoint"
        } else {
          checkpointInterval.toString + " minutes" + " in path " + chkLocation
        }
      }")
    }

    println("\n++++++++++++++++++++++++++++++++++++")
    (j1, j2, checkpointInterval, chkLocation)

  }


}
