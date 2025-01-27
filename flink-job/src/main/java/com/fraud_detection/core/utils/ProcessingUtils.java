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

package com.fraud_detection.core.utils;

import com.fraud_detection.core.entity.Strategy;

import java.util.HashSet;
import java.util.Set;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapState;

public class ProcessingUtils {

    public static void handleStrategyBroadcast(Strategy strategy, BroadcastState<Integer, Strategy> broadcastState) throws Exception {
        switch (strategy.getStrategyState()) {
            case ACTIVE:
            case PAUSE:
                broadcastState.put(strategy.getStrategyId(), strategy);
                break;
            case DELETE:
                broadcastState.remove(strategy.getStrategyId());
                break;
        }
    }

    public static <K, V> Set<V> addToStateValuesSet(MapState<K, Set<V>> mapState, K key, V value) throws Exception {

        Set<V> valuesSet = mapState.get(key);

        if (valuesSet != null) {
            valuesSet.add(value);
        } else {
            valuesSet = new HashSet<>();
            valuesSet.add(value);
        }
        mapState.put(key, valuesSet);
        return valuesSet;
    }
}
