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

package com.fraud_detection.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private final Map<Param<?>, Object> values = new HashMap<>();

    public <T> void put(Param<T> key, T value) {
        values.put(key, value);
    }

    public <T> T get(Param<T> key) {
        return key.getType().cast(values.get(key));
    }

    public Config(Parameters inputParams) {
        overrideDefaults(inputParams, Parameters.STRING_PARAMS);
        overrideDefaults(inputParams, Parameters.INT_PARAMS);
        overrideDefaults(inputParams, Parameters.BOOL_PARAMS);
    }

    public static Config fromParameters(Parameters parameters) {
        return new Config(parameters);
    }

    private <T> void overrideDefaults(Parameters inputParams, List<Param<T>> params) {
        for (Param<T> param : params) {
            put(param, inputParams.getOrDefault(param));
        }
    }
}
