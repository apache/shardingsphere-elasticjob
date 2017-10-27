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

package io.elasticjob.cloud.scheduler.config.app;

import io.elasticjob.util.json.GsonFactory;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.APP_CACHE_ENABLE;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.APP_NAME;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.APP_URL;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.BOOTSTRAP_SCRIPT;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.CPU_COUNT;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.EVENT_TRACE_SAMPLING_COUNT;
import static io.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants.MEMORY_MB;

/**
 * 云作业App配置的Gson工厂.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudAppConfigurationGsonFactory {
    
    static  {
        GsonFactory.registerTypeAdapter(CloudAppConfiguration.class, new CloudAppConfigurationGsonTypeAdapter());
    }
    
    /**
     * 将云作业App配置转换为JSON字符串.
     *
     * @param cloudAppConfig 云作业App配置对象
     * @return 作业配置JSON字符串
     */
    public static String toJson(final CloudAppConfiguration cloudAppConfig) {
        return GsonFactory.getGson().toJson(cloudAppConfig);
    }
    
    /**
     * 将JSON字符串转换为云作业App配置.
     *
     * @param cloudAppConfigJson 云作业App配置JSON字符串
     * @return 作业配置对象
     */
    public static CloudAppConfiguration fromJson(final String cloudAppConfigJson) {
        return GsonFactory.getGson().fromJson(cloudAppConfigJson, CloudAppConfiguration.class);
    }
    
    /**
     * 云作业App配置的Json转换适配器.
     *
     * @author caohao
     */
    public static final class CloudAppConfigurationGsonTypeAdapter extends TypeAdapter<CloudAppConfiguration> {
    
        @Override
        public CloudAppConfiguration read(final JsonReader in) throws IOException {
            String appURL = "";
            String appName = "";
            String bootstrapScript = "";
            double cpuCount = 1.0d;
            double memoryMB = 128.0d;
            boolean appCacheEnable = true;
            int eventTraceSamplingCount = 0;
            in.beginObject();
            while (in.hasNext()) {
                String jsonName = in.nextName();
                switch (jsonName) {
                    case APP_NAME:
                        appName = in.nextString();
                        break;
                    case APP_URL:
                        appURL = in.nextString();
                        break;
                    case BOOTSTRAP_SCRIPT:
                        bootstrapScript = in.nextString();
                        break;
                    case CPU_COUNT:
                        cpuCount = in.nextDouble();
                        break;
                    case MEMORY_MB:
                        memoryMB = in.nextDouble();
                        break;
                    case APP_CACHE_ENABLE:
                        appCacheEnable = in.nextBoolean();
                        break;
                    case EVENT_TRACE_SAMPLING_COUNT:
                        eventTraceSamplingCount = in.nextInt();
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return new CloudAppConfiguration(appName, appURL, bootstrapScript, cpuCount, memoryMB, appCacheEnable, eventTraceSamplingCount);
        }
    
        @Override
        public void write(final JsonWriter out, final CloudAppConfiguration value) throws IOException {
            out.beginObject();
            out.name(APP_NAME).value(value.getAppName());
            out.name(APP_URL).value(value.getAppURL());
            out.name(BOOTSTRAP_SCRIPT).value(value.getBootstrapScript());
            out.name(CPU_COUNT).value(value.getCpuCount());
            out.name(MEMORY_MB).value(value.getMemoryMB());
            out.name(APP_CACHE_ENABLE).value(value.isAppCacheEnable());
            out.name(EVENT_TRACE_SAMPLING_COUNT).value(value.getEventTraceSamplingCount());
            out.endObject();
        }
    }
}
