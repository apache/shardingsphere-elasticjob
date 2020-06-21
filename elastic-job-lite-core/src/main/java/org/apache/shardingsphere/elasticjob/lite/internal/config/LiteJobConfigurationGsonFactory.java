/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.util.json.AbstractJobConfigurationGsonTypeAdapter;
import org.apache.shardingsphere.elasticjob.lite.util.json.GsonFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Job configuration gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteJobConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(LiteJobConfiguration.class, new LiteJobConfigurationGsonTypeAdapter());
    }
    
    /**
     * Transform job configuration to json.
     * 
     * @param liteJobConfig job configuration
     * @return job configuration json string
     */
    public static String toJson(final LiteJobConfiguration liteJobConfig) {
        return GsonFactory.getGson().toJson(liteJobConfig);
    }
    
    /**
     * Transform job configuration to json.
     *
     * @param liteJobConfig job configuration
     * @return job configuration json string
     */
    public static String toJsonForObject(final Object liteJobConfig) {
        return GsonFactory.getGson().toJson(liteJobConfig);
    }
    
    /**
     * Transform json string to job configuration.
     *
     * @param liteJobConfigJson job configuration json string
     * @return job configuration
     */
    public static LiteJobConfiguration fromJson(final String liteJobConfigJson) {
        return GsonFactory.getGson().fromJson(liteJobConfigJson, LiteJobConfiguration.class);
    }
    
    /**
     * Job configuration gson type adapter.
     */
    static final class LiteJobConfigurationGsonTypeAdapter extends AbstractJobConfigurationGsonTypeAdapter<LiteJobConfiguration> {
        
        @Override
        protected void addToCustomizedValueMap(final String jsonName, final JsonReader in, final Map<String, Object> customizedValueMap) throws IOException {
            switch (jsonName) {
                case LiteJobConfigurationConstants.MONITOR_EXECUTION:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.MONITOR_PORT:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE:
                    customizedValueMap.put(jsonName, in.nextString());
                    break;
                case LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case LiteJobConfigurationConstants.DISABLED:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case LiteJobConfigurationConstants.OVERWRITE:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        
        @Override
        protected LiteJobConfiguration getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap) {
            LiteJobConfiguration.Builder builder = LiteJobConfiguration.newBuilder(typeConfig);
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MONITOR_EXECUTION)) {
                builder.monitorExecution((boolean) customizedValueMap.get(LiteJobConfigurationConstants.MONITOR_EXECUTION));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS)) {
                builder.maxTimeDiffSeconds((int) customizedValueMap.get(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.MONITOR_PORT)) {
                builder.monitorPort((int) customizedValueMap.get(LiteJobConfigurationConstants.MONITOR_PORT));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE)) {
                builder.jobShardingStrategyType((String) customizedValueMap.get(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES)) {
                builder.reconcileIntervalMinutes((int) customizedValueMap.get(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.DISABLED)) {
                builder.disabled((boolean) customizedValueMap.get(LiteJobConfigurationConstants.DISABLED));
            }
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.OVERWRITE)) {
                builder.overwrite((boolean) customizedValueMap.get(LiteJobConfigurationConstants.OVERWRITE));
            }
            return builder.build();
        }
        
        @Override
        protected void writeCustomized(final JsonWriter out, final LiteJobConfiguration value) throws IOException {
            out.name(LiteJobConfigurationConstants.MONITOR_EXECUTION).value(value.isMonitorExecution());
            out.name(LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS).value(value.getMaxTimeDiffSeconds());
            out.name(LiteJobConfigurationConstants.MONITOR_PORT).value(value.getMonitorPort());
            out.name(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_TYPE).value(value.getJobShardingStrategyType());
            out.name(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES).value(value.getReconcileIntervalMinutes());
            out.name(LiteJobConfigurationConstants.DISABLED).value(value.isDisabled());
            out.name(LiteJobConfigurationConstants.OVERWRITE).value(value.isOverwrite());
        }
    }
}
