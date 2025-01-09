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

package com.ververica.field.dynamicrules.sources;

import com.ververica.field.config.Config;
import com.ververica.field.dynamicrules.KafkaUtils;
import com.ververica.field.dynamicrules.Rule;
import com.ververica.field.dynamicrules.functions.RuleDeserializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.Properties;

import static com.ververica.field.config.Parameters.RULES_TOPIC;

public class RulesSource {

    private static final int RULES_STREAM_PARALLELISM = 1;

    public static DataStreamSource<String> initRulesSource(Config config, StreamExecutionEnvironment env) {

        Properties kafkaProps = KafkaUtils.initConsumerProperties(config);
        String rulesTopic = config.get(RULES_TOPIC);

        KafkaSource<String> kafkaSource = KafkaSource
                .<String>builder()
                .setProperties(kafkaProps)
                .setTopics(rulesTopic)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // NOTE: Idiomatically, watermarks should be assigned here, but this done later
        // because of the mix of the new Source (Kafka) and SourceFunction-based interfaces.
        // TODO: refactor when FLIP-238 is added
        DataStreamSource<String> dataStreamSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Rules Kafka Source");

        return dataStreamSource;
    }

    public static DataStream<Rule> stringsStreamToRules(DataStream<String> ruleStrings) {
        return ruleStrings
                .flatMap(new RuleDeserializer())
                .name("Rule Deserialization")
                .setParallelism(RULES_STREAM_PARALLELISM)
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                        .<Rule>forBoundedOutOfOrderness(Duration.ofMillis(0))
                        .withTimestampAssigner((event, timestamp) -> Long.MAX_VALUE)
                );
    }
}
