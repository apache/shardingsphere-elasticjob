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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfigurationGsonTypeAdapter;
import com.dangdang.ddframe.job.api.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Lite作业配置的Gson工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class LiteJobConfigurationGsonFactory {
    
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(LiteJobConfiguration.class, new LiteJobConfigurationGsonTypeAdapter()).create();
    
    /**
     * 将作业配置转换为JSON字符串.
     * 
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    public static String toJson(final LiteJobConfiguration liteJobConfig) {
        return GSON.toJson(liteJobConfig);
    }
    
    /**
     * 将作业配置转换为JSON字符串.
     *
     * @param liteJobConfig 作业配置对象
     * @return 作业配置JSON字符串
     */
    // TODO API模块jobSettings使用,未来需调整并删除
    public static String toJsonForObject(final Object liteJobConfig) {
        return GSON.toJson(liteJobConfig);
    }
    
    /**
     * 将JSON字符串转换为作业配置.
     *
     * @param liteJobConfigJson 作业配置JSON字符串
     * @return 作业配置对象
     */
    public static LiteJobConfiguration fromJson(final String liteJobConfigJson) {
        return GSON.fromJson(liteJobConfigJson, LiteJobConfiguration.class);
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
                case "monitorExecution":
                    customizedValueMap.put("monitorExecution", in.nextBoolean());
                    break;
                case "maxTimeDiffSeconds":
                    customizedValueMap.put("maxTimeDiffSeconds", in.nextInt());
                    break;
                case "monitorPort":
                    customizedValueMap.put("monitorPort", in.nextInt());
                    break;
                case "jobShardingStrategyClass":
                    customizedValueMap.put("jobShardingStrategyClass", in.nextString());
                    break;
                case "disabled":
                    customizedValueMap.put("disabled", in.nextBoolean());
                    break;
                case "overwrite":
                    customizedValueMap.put("overwrite", in.nextBoolean());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        
        @Override
        protected LiteJobConfiguration getJobConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap) {
            LiteJobConfiguration.Builder builder = LiteJobConfiguration.newBuilder(typeConfig);
            if (customizedValueMap.containsKey("monitorExecution")) {
                builder.monitorExecution((boolean) customizedValueMap.get("monitorExecution"));
            }
            if (customizedValueMap.containsKey("maxTimeDiffSeconds")) {
                builder.maxTimeDiffSeconds((int) customizedValueMap.get("maxTimeDiffSeconds"));
            }
            if (customizedValueMap.containsKey("monitorPort")) {
                builder.monitorPort((int) customizedValueMap.get("monitorPort"));
            }
            if (customizedValueMap.containsKey("jobShardingStrategyClass")) {
                builder.jobShardingStrategyClass((String) customizedValueMap.get("jobShardingStrategyClass"));
            }
            if (customizedValueMap.containsKey("disabled")) {
                builder.disabled((boolean) customizedValueMap.get("disabled"));
            }
            if (customizedValueMap.containsKey("overwrite")) {
                builder.overwrite((boolean) customizedValueMap.get("overwrite"));
            }
            return builder.build();
        }
    
        @Override
        protected void writeCustomized(final JsonWriter out, final LiteJobConfiguration value) throws IOException {
            out.name("monitorExecution").value(value.isMonitorExecution());
            out.name("maxTimeDiffSeconds").value(value.getMaxTimeDiffSeconds());
            out.name("monitorPort").value(value.getMonitorPort());
            out.name("jobShardingStrategyClass").value(value.getJobShardingStrategyClass());
            out.name("disabled").value(value.isDisabled());
            out.name("overwrite").value(value.isOverwrite());
        }
    }
}
