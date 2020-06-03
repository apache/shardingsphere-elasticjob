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

package io.elasticjob.lite.internal.config;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.elasticjob.lite.config.JobTypeConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.util.json.AbstractJobConfigurationGsonTypeAdapter;
import io.elasticjob.lite.util.json.GsonFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;

/**
 * Lite作业配置的Gson工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteJobConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(LiteJobConfiguration.class, new LiteJobConfigurationGsonTypeAdapter());
    }
    
    /**
     * 将作业配置转换为JSON字符串.
     * 
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    public static String toJson(final LiteJobConfiguration liteJobConfig) {
        return GsonFactory.getGson().toJson(liteJobConfig);
    }
    
    /**
     * 将作业配置转换为JSON字符串.
     *
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    public static String toJsonForObject(final Object liteJobConfig) {
        return GsonFactory.getGson().toJson(liteJobConfig);
    }
    
    /**
     * 将JSON字符串转换为作业配置.
     *
     * @param liteJobConfigJson 作业配置JSON字符串
     * @return 作业配置对象
     */
    public static LiteJobConfiguration fromJson(final String liteJobConfigJson) {
        return GsonFactory.getGson().fromJson(liteJobConfigJson, LiteJobConfiguration.class);
    }
    
    /**
     * Lite作业配置的Json转换适配器.
     *
     * @author zhangliang
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
                case LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_CLASS:
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
            if (customizedValueMap.containsKey(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_CLASS)) {
                builder.jobShardingStrategyClass((String) customizedValueMap.get(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_CLASS));
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
            out.name(LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_CLASS).value(value.getJobShardingStrategyClass());
            out.name(LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES).value(value.getReconcileIntervalMinutes());
            out.name(LiteJobConfigurationConstants.DISABLED).value(value.isDisabled());
            out.name(LiteJobConfigurationConstants.OVERWRITE).value(value.isOverwrite());
        }
    }
}
