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

package com.fraud_detection.sinks;

import com.fraud_detection.config.Config;
import com.fraud_detection.core.utils.KafkaUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import java.util.Arrays;
import java.util.Properties;

import static com.fraud_detection.config.Parameters.LATENCY_SINK;
import static com.fraud_detection.config.Parameters.LATENCY_TOPIC;

public class LatencySink {

    public static DataStreamSink<String> addLatencySink(Config config, DataStream<String> stream) {

        String latencySink = config.get(LATENCY_SINK);
        LatencySink.Type latencySinkType = LatencySink.Type.valueOf(latencySink.toUpperCase());
        DataStreamSink<String> dataStreamSink;

        switch (latencySinkType) {
            case KAFKA:
                Properties kafkaProps = KafkaUtils.initProducerProperties(config);
                String latencyTopic = config.get(LATENCY_TOPIC);
                KafkaSink<String> kafkaSink =
                        KafkaSink.<String>builder()
                                .setKafkaProducerConfig(kafkaProps)
                                .setRecordSerializer(
                                        KafkaRecordSerializationSchema.builder()
                                                .setTopic(latencyTopic)
                                                .setValueSerializationSchema(new SimpleStringSchema())
                                                .build())
                                .setDeliverGuarantee(DeliveryGuarantee.NONE)
                                .build();
                dataStreamSink = stream.sinkTo(kafkaSink);
                break;
            case STDOUT:
                dataStreamSink = stream.addSink(new PrintSinkFunction<>(true));
                break;
            default:
                throw new IllegalArgumentException(
                        "Source \"" + latencySinkType + "\" unknown. Known values are:" + Arrays.toString(Type.values()));
        }
        return dataStreamSink;
    }

    public enum Type {
        KAFKA("Latency Sink (Kafka)"),
        STDOUT("Latency Sink (Std. Out)");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
