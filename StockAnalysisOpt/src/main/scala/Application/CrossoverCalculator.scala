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

import Schemas.{CrossoverToReporterSchema, EMASchema}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.CollectionUtil.iterableToList
import org.apache.flink.util.Collector

class CrossoverCalculator extends KeyedProcessFunction[String, EMASchema, CrossoverToReporterSchema] {

  @transient private var previousEMAjState: ValueState[Double] = _
  @transient private var previousEMAwState: ValueState[Double] = _
  @transient private var listBullishBearish: MapState[Long, (Long, Int)] = _

  def additionManipulation(element: Long, mode: Int): Unit = {
    val keyList: Array[AnyRef] = iterableToList(listBullishBearish.keys()).toArray()

    /**
     * If the history exceeds the last three delete the oldest element
     */
    if (keyList.size >= 3) {

      var minInArray = keyList.head.asInstanceOf[Long]
      keyList.foreach(x => {
        if (x.asInstanceOf[Long] < minInArray) {
          minInArray = x.asInstanceOf[Long]
        }
      })
      listBullishBearish.remove(minInArray)

    }
    listBullishBearish.put(element, (element, mode))


  }

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    previousEMAjState = getRuntimeContext.getState(new ValueStateDescriptor("previousEMAj", classOf[Double]))
    previousEMAwState = getRuntimeContext.getState(new ValueStateDescriptor("previousEMAw", classOf[Double]))
    listBullishBearish = getRuntimeContext.getMapState(new MapStateDescriptor[Long, (Long, Int)]("listBullish", classOf[Long], classOf[(Long, Int)]))
  }

  // TODO for now everything is shown in console. Must figure out how we want to showcase data
  override def processElement(in: EMASchema, ctx: KeyedProcessFunction[String, EMASchema, CrossoverToReporterSchema]#Context, out: Collector[CrossoverToReporterSchema]): Unit = {


    /**
     * Enumeration for Crossover events:
     *  - 0 -> For Bullish
     *  - 1 -> For Bearish
     */


    if (in.WindowClosingPrice != -1.0) {
      /*
        We consider that ema_j is smaller(38) than ema_w(100)
     */
      val previousEMAj = previousEMAjState.value()
      val previousEMAw = previousEMAwState.value()

      if (previousEMAj != 0.0) {
        previousEMAjState.update(in.EMA_j)
        previousEMAwState.update(in.EMA_w)


        if ((previousEMAj <= previousEMAw) && (in.EMA_j > in.EMA_w)) {
          additionManipulation(in.WindowLastTS, 0)
          //          println(s"Bullish Breakout for key ${in.Symbol}, PREVIOUS[j ${previousEMAj}, w ${previousEMAw}] AND CURRENT [j ${in.EMA_j}, w ${in.EMA_w}] ")
        }

        if ((previousEMAj >= previousEMAw) && (in.EMA_j < in.EMA_w)) {
          additionManipulation(in.WindowLastTS, 1)
          //          println(s"Bearish Breakout for key ${in.Symbol}, PREVIOUS[j ${previousEMAj}, w ${previousEMAw}] AND CURRENT [j ${in.EMA_j}, w ${in.EMA_w}] ")

        }

      } else {
        previousEMAjState.update(in.EMA_j)
        previousEMAwState.update(in.EMA_w)
      }
    }

    val iteratorList: Array[AnyRef] = iterableToList(listBullishBearish.values()).toArray()

    out.collect(CrossoverToReporterSchema(Symbol = in.Symbol, securityType = in.security_type, batchID = in.batchID,
      lookupSymbolBool = in.lookupSymbolBool, lookupSize = in.lookupSize,
      WindowStartTS = in.WindowStartTS, WindowLastTS = in.WindowLastTS, WindowClosingPrice = in.WindowClosingPrice,
      CrossoverList = iteratorList, isLastWindow = in.isLastWindow))


  }


}