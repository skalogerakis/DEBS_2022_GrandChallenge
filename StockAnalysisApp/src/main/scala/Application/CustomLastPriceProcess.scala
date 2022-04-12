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

import Schemas.{BatchInternalSchema, EventUnpackSchema, LastPriceOutputSchema, WindowInternalSchema}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.CollectionUtil.iterableToList
import org.apache.flink.util.Collector

class CustomLastPriceProcess() extends KeyedProcessFunction[String, EventUnpackSchema, LastPriceOutputSchema] {

  private var windowState: MapState[Long, WindowInternalSchema] = _
  private var batchState: MapState[Long, BatchInternalSchema] = _

  /*
    Use this to extract from current timestamp, the window that it belongs in a 5-minute window. It includes entries from [0,5)
   */
  def windowGrouping(currentTS: Long): Long = {
    /*
       This is used in order to add 5 minutes to current long TS
       Formal expression: f(time) = math.floor(time/interval) * interval
     */
    math.floor(currentTS / (5 * 60 * 1000)).toLong * (5 * 60 * 1000)
  }


  override def open(parameters: Configuration): Unit = {
    windowState = getRuntimeContext.getMapState(new MapStateDescriptor[Long, WindowInternalSchema]("windowState", classOf[Long], classOf[WindowInternalSchema]))
    batchState = getRuntimeContext.getMapState(new MapStateDescriptor[Long, BatchInternalSchema]("batchState", classOf[Long], classOf[BatchInternalSchema]))
  }

  override def processElement(
                               i: EventUnpackSchema,
                               ctx: KeyedProcessFunction[String, EventUnpackSchema, LastPriceOutputSchema]#Context,
                               out: Collector[LastPriceOutputSchema]): Unit = {

    /*
    In case a symbol is only lookup and does not exist in a current batch simply proceed
     */
    if (i.Price == -1.0) {
      out.collect(LastPriceOutputSchema(Symbol = i.Symbol, batchID = i.batchID, securityType = i.securityType,
        lookupSymbolBool = i.lookupSymbolBool, lookupSize = i.lookupSize,
        WindowLastTS = i.Timestamp, WindowClosingPrice = -1.0, isLastBatch = i.isLastBatch,
        isLastWindow = true))
    }
    else {

      val windowGroupRunningTS = windowGrouping(i.Timestamp)

      /**
       * +++++++++INITIALIZATION STAGE+++++++++
       */

      if (!batchState.contains(i.batchID)) {
        batchState.put(i.batchID, BatchInternalSchema(isClosed = false, hasSent = false,
          isLookupBool = i.lookupSymbolBool, lookupSize = i.lookupSize, isLastBatch = i.isLastBatch))
      }

      /*
        Initialization if there is not the desired window.
        Aligns everything to the correct place and assigns the starting window time
       */
      if (!windowState.contains(windowGroupRunningTS)) {
        windowState.put(windowGroupRunningTS, WindowInternalSchema(idWindowStart = windowGroupRunningTS,
          last_timestamp = i.Timestamp, last_price = i.Price))
      }


      /**
       * +++++++++Calculation of last price per window+++++++++
       */

      if (windowState.get(windowGroupRunningTS).last_timestamp < i.Timestamp) {
        windowState.put(windowGroupRunningTS, windowState.get(windowGroupRunningTS).copy(last_timestamp = i.Timestamp, last_price = i.Price))

      }




      /*
        If this is the last element of the Bi, then consider it done
       */
      if (i.LastInBiBool) {
        /*
          Consider that the batch is closed
         */

        batchState.put(i.batchID, batchState.get(i.batchID) _ActivateIsClosed)

        val currentWindowTotalTS: Long = windowGrouping(i.totalBatchTimestamp)

        val windowStateKeys = iterableToList(windowState.keys())
        val completedWindowList = windowStateKeys.stream().filter(value => value.longValue() < currentWindowTotalTS).sorted().toArray()


        var completedWindowCounter: Int = 0
        val completedWindowSize: Int = completedWindowList.size

        completedWindowList.foreach(elem => {
          completedWindowCounter += 1
          val tempWindowState: WindowInternalSchema = windowState.get(elem.asInstanceOf[Long])
          val curBatchState: BatchInternalSchema = batchState.get(i.batchID)

          out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = i.batchID,
            lookupSymbolBool = curBatchState.isLookupBool, lookupSize = curBatchState.lookupSize,
            WindowLastTS = tempWindowState.last_timestamp, WindowClosingPrice = tempWindowState.last_price,
            isLastBatch = curBatchState.isLastBatch,
            isLastWindow = if (completedWindowCounter == completedWindowSize) true else false))

          /*
             Remove all the completed window and mark that the batch hasSent
           */
          windowState.remove(elem.asInstanceOf[Long])
          batchState.put(i.batchID, curBatchState _ActivateHasSent)


        })

        /*
          In the simple case at most one uncompleted window, no need for complex processing
         */

        val curBatchState: BatchInternalSchema = batchState.get(i.batchID)
        if (!curBatchState.hasSent && curBatchState.isLookupBool) {
          val uncompletedWindow: WindowInternalSchema = windowState.get(currentWindowTotalTS)
          out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = i.batchID, lookupSymbolBool = curBatchState.isLookupBool,
            lookupSize = curBatchState.lookupSize,
            WindowLastTS = uncompletedWindow.last_timestamp, WindowClosingPrice = -1.0,
            isLastBatch = curBatchState.isLastBatch, isLastWindow = true))

          batchState.put(i.batchID, curBatchState _ActivateHasSent)
        }


      }
    }

  }
}