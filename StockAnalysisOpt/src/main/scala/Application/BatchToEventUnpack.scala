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

import Schemas.EventUnpackSchema
import Utilities.RedisStandaloneClient
import com.google.protobuf.Timestamp
import com.google.protobuf.util.Timestamps
import grpc.modules.{Batch, SecurityType}
import io.lettuce.core.api.sync.RedisCommands
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.shaded.guava30.com.google.common.hash.{BloomFilter, Funnels}
import org.apache.flink.util.Collector

import java.nio.charset.Charset
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.collection.mutable.HashMap

class BatchToEventUnpack extends RichFlatMapFunction[Batch, EventUnpackSchema] {
  private var synCommand: RedisCommands[String, Object] = null;

  override def open(parameters: Configuration): Unit = {
    val redisClient = new RedisStandaloneClient()
    redisClient.initialize()
    synCommand = redisClient.syncGetter()

  }

  override def flatMap(batch: Batch, collector: Collector[EventUnpackSchema]): Unit = {
    val lookupSymbols: Set[String] = batch.getLookupSymbolsList.toSet

    var bloomFilter: BloomFilter[String] = BloomFilter.create[String](Funnels.stringFunnel(Charset.forName("UTF-8")), batch.getEventsList.size() * 10, 0.0001)


    /**
     * Preprocessing step involves scanning all the events once in a batch to find:
     *  - Total Timestamp of batch
     *  - Last in Bi
     *  - If a symbol is lookup
     *  - The number of lookup symbols
     */

    val totalBatchTS: Timestamp = batch.getEventsList.get(batch.getEventsList.size() - 1).getLastTrade
    var distinctEventMap: HashMap[String, Int] = HashMap.empty[String, Int]


    var eventCounter: Int = 0
    batch.getEventsList.forEach(event => {
      distinctEventMap(event.getSymbol) = eventCounter
      eventCounter += 1
    })

    /**
     * Insert all the distinct symbols in the bloom filter
     */
    distinctEventMap.foreach(w1 => bloomFilter.put(w1._1))


    /**
     * In case one of the lookupSymbols does not exist in the batch, then add a dummy value to get the correct results
     */
    lookupSymbols.foreach(ls => {
      if (!distinctEventMap.contains(ls)) {
        bloomFilter.put(ls)
        collector.collect(EventUnpackSchema(Symbol = ls, securityType = SecurityType.Equity, Price = -1.0,
          Timestamp = Timestamps.toMillis(batch.getEventsList.get(0).getLastTrade), batchID = batch.getSeqId, totalBatchTimestamp = Timestamps.toMillis(totalBatchTS),
          LastInBiBool = true, lookupSymbolBool = true, lookupSize = lookupSymbols.size,
          isLastBatch = batch.getLast))
      }

    })

    /**
     * Set the bloom filter to the corresponding batchId
     */
    synCommand.hset(batch.getSeqId.toString, "BloomFilter", bloomFilter)

    var newEventCounter: Int = 0
    batch.getEventsList.forEach(b => {

      val lastInBi: Boolean = if (newEventCounter == distinctEventMap.getOrElse(b.getSymbol, 0).toInt) {
        true
      } else {
        false
      }

      collector.collect(EventUnpackSchema(Symbol = b.getSymbol, securityType = b.getSecurityType, Price = b.getLastTradePrice,
        Timestamp = Timestamps.toMillis(b.getLastTrade), batchID = batch.getSeqId, totalBatchTimestamp = Timestamps.toMillis(totalBatchTS),
        LastInBiBool = lastInBi, lookupSymbolBool = lookupSymbols.contains(b.getSymbol), lookupSize = lookupSymbols.size,
        isLastBatch = batch.getLast))

      newEventCounter += 1

    }
    )
  }
}
