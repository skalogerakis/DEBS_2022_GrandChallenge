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

import Serializers.SerializedObjectCodec
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisStandaloneClient() {

  private var redisClient: RedisClient = null
  private var connection: StatefulRedisConnection[String, Object] = null
  private var sync: RedisCommands[String, Object] = null

  /**
   * Init both connection and client
   */
  def initialize(host: String = "localhost"): Unit = {
    redisClient = RedisClient.create("redis://" + host + ":6379")
    connection = redisClient.connect(new SerializedObjectCodec());
    sync = connection.sync();
    println(s"Connected to Redis single node on node ${host}")

  }

  def syncGetter(): RedisCommands[String, Object] = {
    sync
  }

  /**
   * Shutdown both connection and client
   */
  def shutdown(): Unit = {
    if (connection != null) {
      connection.close()
    }

    if (redisClient != null) {
      redisClient.shutdown()
    }
  }


}
