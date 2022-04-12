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
import Utilities.RedisStandaloneClient
import io.lettuce.core.api.sync.RedisCommands
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.shaded.guava30.com.google.common.hash.BloomFilter
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.CollectionUtil.iterableToList
import org.apache.flink.util.Collector

import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

class CustomLastPriceProcess() extends KeyedProcessFunction[String, EventUnpackSchema, LastPriceOutputSchema] {

  @transient private var windowState: MapState[Long, WindowInternalSchema] = _
  @transient private var batchState: MapState[Long, BatchInternalSchema] = _
  @transient private var flagLast: ValueState[Boolean] = _
  @transient private var syncCommand: RedisCommands[String, Object] = null;

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
    flagLast = getRuntimeContext.getState(new ValueStateDescriptor("flagLast", classOf[Boolean]))

    val redisClient: RedisStandaloneClient = new RedisStandaloneClient()
    redisClient.initialize()
    syncCommand = redisClient.syncGetter()
  }

  /**
   * TODO
   *    - TODO maybe turn for loop to while
   *    - TODO Prune the list of batches
   */
  override def processElement(
                               i: EventUnpackSchema,
                               ctx: KeyedProcessFunction[String, EventUnpackSchema, LastPriceOutputSchema]#Context,
                               out: Collector[LastPriceOutputSchema]): Unit = {

    /*
    The price is equal to -1.0 when the lookupSymbol does not exist in the batch, so proceed
    */
    if (i.Price == -1.0) {

      out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = i.batchID,
        lookupSymbolBool = i.lookupSymbolBool, lookupSize = i.lookupSize, WindowStartTS = i.Timestamp,
        WindowLastTS = i.Timestamp, WindowClosingPrice = -1.0, WindowAffectedBatches = Set(),
        isLastBatch = i.isLastBatch, isLastWindow = true))

      // We are also certain that the there is not another entry with the specific symbol in that batch, so mark as sent
      batchState.put(i.batchID, BatchInternalSchema(isClosed = true, hasSent = true, isLookupBool = i.lookupSymbolBool,
        lookupSize = i.lookupSize, isLastBatch = i.isLastBatch))


    }
    else {
      val currentWindowGroup: Long = windowGrouping(i.Timestamp)

      /**
       * Initialization Stage
       */

      if (!batchState.contains(i.batchID)) {
        batchState.put(i.batchID, BatchInternalSchema(isClosed = false, hasSent = false, isLookupBool = i.lookupSymbolBool,
          lookupSize = i.lookupSize, isLastBatch = i.isLastBatch))
      }


      /*
        Initialization if there is not the desired window. Aligns everything to the correct place and assigns the starting window time
       */
      if (!windowState.contains(currentWindowGroup)) {
        windowState.put(currentWindowGroup, WindowInternalSchema(idWindowStart = currentWindowGroup, last_timestamp = i.Timestamp,
          last_price = i.Price, affectedBatches = Set(i.batchID), canSendMap = mutable.HashMap(i.batchID -> i.LastInBiBool)))
      }


      /**
       * Calculation of last price per window
       */
      val tempWindowGroup: WindowInternalSchema = windowState.get(currentWindowGroup)
      if (tempWindowGroup.last_timestamp < i.Timestamp) {
        windowState.put(currentWindowGroup, tempWindowGroup.copy(last_timestamp = i.Timestamp, last_price = i.Price))
      }

      val tempWindowGroup2: WindowInternalSchema = windowState.get(currentWindowGroup) //Re-define the window state so that it is updated
      if (!tempWindowGroup2.affectedBatches.contains(i.batchID) && i.LastInBiBool) {
        windowState.put(currentWindowGroup, tempWindowGroup2.copy(affectedBatches = tempWindowGroup2.affectedBatches + i.batchID,
          canSendMap = tempWindowGroup2.canSendMap += (i.batchID -> i.LastInBiBool)))
      }


      /*
        If this is the last element of the Bi, then consider it done
       */
      if (i.LastInBiBool) {
        /*
          Consider that the batch is closed
         */
        batchState.put(i.batchID, batchState.get(i.batchID) _ActivateIsClosed)


        val batchLst = iterableToList(batchState.keys()).stream().sorted().toArray()
        val tail = batchLst.last.asInstanceOf[Long]

        var canComplete = true
        var canCompleteLast = false

        breakable {
          for (j <- 0.toLong to i.batchID) {
            val currentBatchState0: BatchInternalSchema = batchState.get(j)
            if (!batchState.contains(j) || !currentBatchState0.isClosed) {

              if (syncCommand.hget(j.toString, "BloomFilter") == null || currentBatchState0 != null) {
                canComplete = false
                break
              }

              if (!syncCommand.hget(j.toString, "BloomFilter").asInstanceOf[BloomFilter[String]].mightContain(i.Symbol)) {
                batchState.put(j, BatchInternalSchema(isClosed = true, hasSent = true, isLookupBool = false,
                  lookupSize = 0, isLastBatch = false))
              } else {
                canComplete = false
                break;
              }
            }
          }
        }


        /*
          This activates when we find the last element.
         */
        if (batchState.get(i.batchID).isLastBatch && !flagLast.value()) {
          flagLast.update(true)
        }

        /*
        In case we have found the last element, scan all the remaining element to check whether they have completed.
         */
        if (flagLast.value() && canComplete) {
          canCompleteLast = true

          breakable {
            for (w <- i.batchID to tail) {
              val currentBatchState: BatchInternalSchema = batchState.get(w)
              if (!batchState.contains(w) || !currentBatchState.isClosed) {
                if (syncCommand.hget(w.toString, "BloomFilter") == null || currentBatchState != null) {
                  canCompleteLast = false
                  break
                }

                if (!syncCommand.hget(w.toString, "BloomFilter").asInstanceOf[BloomFilter[String]].mightContain(i.Symbol)) {
                  batchState.put(w, BatchInternalSchema(isClosed = true, hasSent = true, isLookupBool = false,
                    lookupSize = 0, isLastBatch = false))
                } else {
                  canCompleteLast = false
                  break
                }
              }
            }
          }

        }
        //TODO check until here


        val currentMaxWindow: Long = windowGrouping(i.totalBatchTimestamp)
        val windowStateKeys = iterableToList(windowState.keys())
        val completedWindowList = windowStateKeys.stream().filter(value => value.longValue() < currentMaxWindow).sorted().toArray()

        /**
         * This happens when there are windows that are ready to close and have all their batches completed
         */
        if (canComplete && completedWindowList.nonEmpty) {

          var completedWindowCounter: Int = 0
          val completedWindowSize: Int = completedWindowList.size

          var prevState = if (completedWindowList.nonEmpty)
            windowState.get(completedWindowList.head.asInstanceOf[Long])

          completedWindowList.foreach(elem => {
            completedWindowCounter += 1

            val tempWindowState: WindowInternalSchema = windowState.get(elem.asInstanceOf[Long])
            val canSendList = tempWindowState.canSendMap
            val affectedList = tempWindowState.affectedBatches.toSeq.sorted

            affectedList.foreach(seq => {
              val tempBatchStateSeq: BatchInternalSchema = batchState.get(seq)
              if (!tempBatchStateSeq.hasSent && seq == affectedList.head && completedWindowCounter != 1) {
                out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = seq,
                  lookupSymbolBool = tempBatchStateSeq.isLookupBool, lookupSize = tempBatchStateSeq.lookupSize,
                  WindowStartTS = prevState.asInstanceOf[WindowInternalSchema].idWindowStart,
                  WindowLastTS = prevState.asInstanceOf[WindowInternalSchema].last_timestamp,
                  WindowClosingPrice = prevState.asInstanceOf[WindowInternalSchema].last_price,
                  WindowAffectedBatches = prevState.asInstanceOf[WindowInternalSchema].affectedBatches,
                  isLastBatch = tempBatchStateSeq.isLastBatch, isLastWindow = canSendList(seq)))

                if (canSendList(seq)) {
                  batchState.put(seq, tempBatchStateSeq _ActivateHasSent)
                }

              } else if (!tempBatchStateSeq.hasSent && tempBatchStateSeq.isLookupBool && canSendList(seq)) {
                val tempElemSend: WindowInternalSchema = windowState.get(elem.asInstanceOf[Long])
                out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = seq,
                  lookupSymbolBool = tempBatchStateSeq.isLookupBool, lookupSize = tempBatchStateSeq.lookupSize,
                  WindowStartTS = tempElemSend.idWindowStart, WindowLastTS = tempElemSend.last_timestamp,
                  WindowClosingPrice = -1.0, WindowAffectedBatches = tempElemSend.affectedBatches,
                  isLastBatch = tempBatchStateSeq.isLastBatch, isLastWindow = canSendList(seq)))

                batchState.put(seq, tempBatchStateSeq _ActivateHasSent)

              }
            })

            /**
             * This is used to update the triggering batch, when all the previous windows have completed updating
             */
            if (completedWindowCounter == completedWindowSize) {
              //Get the first affected current window
              val firstAffectedCurrentWindow: Long = windowState.get(windowGrouping(i.Timestamp)).affectedBatches.toSeq.sorted.head
              val batchFirstAffectedCurWindow: BatchInternalSchema = batchState.get(firstAffectedCurrentWindow)
              val tempElemSend2: WindowInternalSchema = windowState.get(elem.asInstanceOf[Long])

              out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType,
                batchID = firstAffectedCurrentWindow, lookupSymbolBool = batchFirstAffectedCurWindow.isLookupBool,
                lookupSize = batchFirstAffectedCurWindow.lookupSize, WindowStartTS = tempElemSend2.idWindowStart,
                WindowLastTS = tempElemSend2.last_timestamp, WindowClosingPrice = tempElemSend2.last_price,
                WindowAffectedBatches = tempElemSend2.affectedBatches, isLastBatch = batchFirstAffectedCurWindow.isLastBatch, isLastWindow = true))

              batchState.put(firstAffectedCurrentWindow, batchFirstAffectedCurWindow _ActivateHasSent)

            }


            /*
               Remove all the completed window and mark that the batch hasSent
             */
            prevState = tempWindowState
            windowState.remove(elem.asInstanceOf[Long]) //Once calculated everything discard that window


          })
        }


        /**
         * In the case the window cannot close, check whether any batches of the current window can get released
         * This will happen if there are no extra dependencies.
         */

        val curBatchState: BatchInternalSchema = batchState.get(i.batchID)
        if (!curBatchState.hasSent && curBatchState.isLookupBool) {
          val uncompletedWindow: WindowInternalSchema = windowState.get(currentMaxWindow)
          val windowListIterator = iterableToList(windowState.keys())
          val completedWindowList = windowListIterator.stream().filter(value => value.longValue() < currentMaxWindow).toArray()


          // If array is empty it means no dependency that is required to release results of the current window
          if (completedWindowList.isEmpty) {
            out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType, batchID = i.batchID,
              lookupSymbolBool = curBatchState.isLookupBool, lookupSize = curBatchState.lookupSize,
              WindowStartTS = uncompletedWindow.idWindowStart, WindowLastTS = uncompletedWindow.last_timestamp,
              WindowClosingPrice = -1.0, WindowAffectedBatches = uncompletedWindow.affectedBatches,
              isLastBatch = curBatchState.isLastBatch, isLastWindow = true))

            batchState.put(i.batchID, curBatchState _ActivateHasSent)

          }


        }


        /**
         * In case we found the last batch and all the rest batches have completed.
         * Very similar to the completed window case, but its better to keep everything discrete
         */
        if (canCompleteLast) {

          val remainingWindowStateKeys = iterableToList(windowState.keys())
          val remainingCompletedWindowList = remainingWindowStateKeys.stream().sorted().toArray()

          var prevState = if (!remainingCompletedWindowList.isEmpty)
            windowState.get(remainingCompletedWindowList.head.asInstanceOf[Long])


          remainingCompletedWindowList.foreach(elem => {
            val tempWindowState: WindowInternalSchema = windowState.get(elem.asInstanceOf[Long])
            val canSendList = tempWindowState.canSendMap
            val affectedBatchesArray: Array[Long] = tempWindowState.affectedBatches.toArray.sorted

            affectedBatchesArray.foreach(batch => {
              val curBatchState: BatchInternalSchema = batchState.get(batch)

              if (!curBatchState.hasSent && batch == affectedBatchesArray.head) {
                out.collect(LastPriceOutputSchema(Symbol = i.Symbol, batchID = batch, securityType = i.securityType,
                  lookupSymbolBool = curBatchState.isLookupBool, lookupSize = curBatchState.lookupSize,
                  WindowStartTS = prevState.asInstanceOf[WindowInternalSchema].idWindowStart,
                  WindowLastTS = prevState.asInstanceOf[WindowInternalSchema].last_timestamp,
                  WindowClosingPrice = prevState.asInstanceOf[WindowInternalSchema].last_price,
                  WindowAffectedBatches = prevState.asInstanceOf[WindowInternalSchema].affectedBatches,
                  isLastBatch = curBatchState.isLastBatch, isLastWindow = canSendList(batch)))

                if (canSendList(batch)) {
                  batchState.put(batch, curBatchState _ActivateHasSent)
                }

              } else if (!batchState.get(batch).hasSent && canSendList(batch) && curBatchState.isLookupBool) {
                out.collect(LastPriceOutputSchema(Symbol = i.Symbol, securityType = i.securityType,
                  batchID = batch, lookupSymbolBool = curBatchState.isLookupBool, lookupSize = curBatchState.lookupSize,
                  WindowStartTS = tempWindowState.idWindowStart, WindowLastTS = tempWindowState.last_timestamp,
                  WindowClosingPrice = -1.0, WindowAffectedBatches = tempWindowState.affectedBatches,
                  isLastBatch = curBatchState.isLastBatch, isLastWindow = canSendList(batch)))
                batchState.put(batch, curBatchState _ActivateHasSent)

              }

            })
            prevState = tempWindowState
            windowState.remove(elem.asInstanceOf[Long])

          })


        }


      }
    }

  }
}