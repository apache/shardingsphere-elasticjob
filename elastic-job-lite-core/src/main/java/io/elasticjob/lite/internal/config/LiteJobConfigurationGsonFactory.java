/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
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

import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.DISABLED;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.JOB_SHARDING_STRATEGY_CLASS;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.MAX_TIME_DIFF_SECONDS;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.MONITOR_EXECUTION;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.MONITOR_PORT;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.OVERWRITE;
import static io.elasticjob.lite.internal.config.LiteJobConfigurationConstants.RECONCILE_INTERVAL_MINUTES;

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
                case MONITOR_EXECUTION:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case MAX_TIME_DIFF_SECONDS:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case MONITOR_PORT:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case JOB_SHARDING_STRATEGY_CLASS:
                    customizedValueMap.put(jsonName, in.nextString());
                    break;
                case RECONCILE_INTERVAL_MINUTES:
                    customizedValueMap.put(jsonName, in.nextInt());
                    break;
                case DISABLED:
                    customizedValueMap.put(jsonName, in.nextBoolean());
                    break;
                case OVERWRITE:
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
            if (customizedValueMap.containsKey(MONITOR_EXECUTION)) {
                builder.monitorExecution((boolean) customizedValueMap.get(MONITOR_EXECUTION));
            }
            if (customizedValueMap.containsKey(MAX_TIME_DIFF_SECONDS)) {
                builder.maxTimeDiffSeconds((int) customizedValueMap.get(MAX_TIME_DIFF_SECONDS));
            }
            if (customizedValueMap.containsKey(MONITOR_PORT)) {
                builder.monitorPort((int) customizedValueMap.get(MONITOR_PORT));
            }
            if (customizedValueMap.containsKey(JOB_SHARDING_STRATEGY_CLASS)) {
                builder.jobShardingStrategyClass((String) customizedValueMap.get(JOB_SHARDING_STRATEGY_CLASS));
            }
            if (customizedValueMap.containsKey(RECONCILE_INTERVAL_MINUTES)) {
                builder.reconcileIntervalMinutes((int) customizedValueMap.get(RECONCILE_INTERVAL_MINUTES));
            }
            if (customizedValueMap.containsKey(DISABLED)) {
                builder.disabled((boolean) customizedValueMap.get(DISABLED));
            }
            if (customizedValueMap.containsKey(OVERWRITE)) {
                builder.overwrite((boolean) customizedValueMap.get(OVERWRITE));
            }
            return builder.build();
        }
        
        @Override
        protected void writeCustomized(final JsonWriter out, final LiteJobConfiguration value) throws IOException {
            out.name(MONITOR_EXECUTION).value(value.isMonitorExecution());
            out.name(MAX_TIME_DIFF_SECONDS).value(value.getMaxTimeDiffSeconds());
            out.name(MONITOR_PORT).value(value.getMonitorPort());
            out.name(JOB_SHARDING_STRATEGY_CLASS).value(value.getJobShardingStrategyClass());
            out.name(RECONCILE_INTERVAL_MINUTES).value(value.getReconcileIntervalMinutes());
            out.name(DISABLED).value(value.isDisabled());
            out.name(OVERWRITE).value(value.isOverwrite());
        }
    }
}
