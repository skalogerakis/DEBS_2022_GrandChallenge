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

import Schemas.EMASchema
import grpc.modules.{Indicator, ResultQ1}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import java.time.LocalDateTime

class ReportQ1 extends KeyedProcessFunction[Long, EMASchema, ResultQ1] {

  @transient private var eventCounter: ValueState[Int] = _
  @transient private var flagResults: ValueState[Boolean] = _
  @transient private var resultList: ListState[Indicator] = _

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    eventCounter = getRuntimeContext.getState(new ValueStateDescriptor("eventCounter", classOf[Int]))
    flagResults = getRuntimeContext.getState(new ValueStateDescriptor("flagResults", classOf[Boolean]))
    resultList = getRuntimeContext.getListState(new ListStateDescriptor[Indicator]("resultList", classOf[Indicator]))
  }

  // TODO must see how data is collected
  override def processElement(in: EMASchema, ctx: KeyedProcessFunction[Long, EMASchema, ResultQ1]#Context, out: Collector[ResultQ1]): Unit = {

    if (in.isLastWindow && in.lookupSymbolBool) {

      val increment: Int = eventCounter.value() + 1
      eventCounter.update(increment)

      val indicator: Indicator = Indicator.newBuilder()
        .setSymbol(in.Symbol)
        .setEma38(in.EMA_w.toFloat)
        .setEma100(in.EMA_w.toFloat)
        .build()

      resultList.add(indicator)

      if (eventCounter.value() == in.lookupSize) {
        flagResults.update(true)
      }
    }

    if (flagResults.value()) {

      if (in.batchID % 100 == 0){
        println(s"REPORT Q1 ->Batch ID: ${in.batchID}, Timestamp ${LocalDateTime.now()}")
      }
      val resultQ1 = ResultQ1.newBuilder()
        .setBatchSeqId(ctx.getCurrentKey)
        .addAllIndicators(resultList.get())
        .build();

      out.collect(resultQ1)
      flagResults.update(false)

    }


  }


}

