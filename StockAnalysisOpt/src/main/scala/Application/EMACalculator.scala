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

import Schemas.{EMASchema, LastPriceOutputSchema}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration


class EMACalculator(j: Double = 38.0, w: Double = 100.0) extends RichMapFunction[LastPriceOutputSchema, EMASchema] {

  private var lastEMAj: ValueState[Double] = _
  private var lastEMAw: ValueState[Double] = _

  override def open(parameters: Configuration): Unit = {
    lastEMAj = getRuntimeContext.getState(new ValueStateDescriptor[(Double)]("lastEMAj", classOf[(Double)]))
    lastEMAw = getRuntimeContext.getState(new ValueStateDescriptor[(Double)]("lastEMAw", classOf[(Double)]))
  }

  override def map(in: LastPriceOutputSchema): EMASchema = {

    /*
      Calculate ema_j and ema_w. The initial value of the ValueState is not null but 0.0 so everything is more simple.
      The first case a new window is not triggered, and simply return the latest EMA prices for reporting in the next stage.
      The second case calculates the new EMA.
     */

    if (in.WindowClosingPrice == -1.0) {
      EMASchema(Symbol = in.Symbol, security_type = in.securityType, batchID = in.batchID, lookupSymbolBool = in.lookupSymbolBool,
        lookupSize = in.lookupSize, WindowStartTS = in.WindowStartTS, WindowLastTS = in.WindowLastTS, WindowClosingPrice = in.WindowClosingPrice,
        isLastBatch = in.isLastBatch, EMA_j = lastEMAj.value(), EMA_w = lastEMAw.value(), isLastWindow = in.isLastWindow)
    } else {
      val ema_j: Double = emaCalc(in.WindowClosingPrice, lastEMAj.value(), j)
      lastEMAj.update(ema_j)
      val ema_w: Double = emaCalc(in.WindowClosingPrice, lastEMAw.value(), w)
      lastEMAw.update(ema_w)

      EMASchema(Symbol = in.Symbol, security_type = in.securityType, batchID = in.batchID, lookupSymbolBool = in.lookupSymbolBool,
        lookupSize = in.lookupSize, WindowStartTS = in.WindowStartTS, WindowLastTS = in.WindowLastTS, WindowClosingPrice = in.WindowClosingPrice,
        isLastBatch = in.isLastBatch, EMA_j = ema_j, EMA_w = ema_w, isLastWindow = in.isLastWindow)
    }


  }

  def emaCalc(LastPrice: Double, lastEMA: Double, param: Double): Double = {
    LastPrice * (2 / (1 + param)) + lastEMA * (1 - (2 / (1 + param)))
  }


}
