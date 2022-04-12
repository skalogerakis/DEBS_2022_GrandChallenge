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

package Utilities

import Schemas.CrossoverToReporterSchema
import com.google.protobuf.util.Timestamps.fromMillis
import grpc.modules.CrossoverEvent.SignalType
import grpc.modules.{CrossoverEvent, ResultQ2}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

class ReportQ2() extends KeyedProcessFunction[Long, CrossoverToReporterSchema, ResultQ2] {

  @transient private var eventCounter: ValueState[Int] = _
  @transient private var resultList: ListState[(Long, Int)] = _
  @transient private var flagResults: ValueState[Boolean] = _
  @transient private var resultListQ2: ListState[CrossoverEvent] = _

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    eventCounter = getRuntimeContext.getState(new ValueStateDescriptor("eventCounter", classOf[Int]))
    flagResults = getRuntimeContext.getState(new ValueStateDescriptor("flagResults", classOf[Boolean]))
    resultList = getRuntimeContext.getListState(new ListStateDescriptor[(Long, Int)]("collectionEMAj", classOf[(Long, Int)]))
    resultListQ2 = getRuntimeContext.getListState(new ListStateDescriptor[CrossoverEvent]("resultListQ2", classOf[CrossoverEvent]))

  }

  override def processElement(in: CrossoverToReporterSchema, ctx: KeyedProcessFunction[Long, CrossoverToReporterSchema, ResultQ2]#Context, out: Collector[ResultQ2]): Unit = {

    if (in.isLastWindow && in.lookupSymbolBool) {
      val increment: Int = eventCounter.value() + 1
      eventCounter.update(increment)

      in.CrossoverList.foreach(crossEvent => {

        val cross = CrossoverEvent.newBuilder()
          .setSymbol(in.Symbol)
          .setSecurityType(in.securityType)
          .setSignalType(SignalType.forNumber(crossEvent.asInstanceOf[(Long, Int)]._2))
          .setTs(fromMillis(crossEvent.asInstanceOf[(Long, Int)]._1))
          .build()

        resultListQ2.add(cross)
      })


      if (eventCounter.value() == in.lookupSize) {
        flagResults.update(true)
      }
    }


    if (flagResults.value()) {
      //      println(s"RESULTS For batch ${ctx.getCurrentKey}, For symbol ${in.Symbol}")

      val resultQ2 = ResultQ2.newBuilder()
        .setBatchSeqId(ctx.getCurrentKey)
        .addAllCrossoverEvents(resultListQ2.get())
        .build();

      out.collect(resultQ2)
      flagResults.update(false)

    }

  }


}

