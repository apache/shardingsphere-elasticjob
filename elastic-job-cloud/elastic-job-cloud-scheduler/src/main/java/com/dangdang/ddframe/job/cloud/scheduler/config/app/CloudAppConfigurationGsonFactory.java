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

package com.dangdang.ddframe.job.cloud.scheduler.config.app;

import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

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
                    case "appName":
                        appName = in.nextString();
                        break;
                    case "appURL":
                        appURL = in.nextString();
                        break;
                    case "bootstrapScript":
                        bootstrapScript = in.nextString();
                        break;
                    case "cpuCount":
                        cpuCount = in.nextDouble();
                        break;
                    case "memoryMB":
                        memoryMB = in.nextDouble();
                        break;
                    case "appCacheEnable":
                        appCacheEnable = in.nextBoolean();
                        break;
                    case "eventTraceSamplingCount":
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
            out.name("appName").value(value.getAppName());
            out.name("appURL").value(value.getAppURL());
            out.name("bootstrapScript").value(value.getBootstrapScript());
            out.name("cpuCount").value(value.getCpuCount());
            out.name("memoryMB").value(value.getMemoryMB());
            out.name("appCacheEnable").value(value.isAppCacheEnable());
            out.name("eventTraceSamplingCount").value(value.getEventTraceSamplingCount());
            out.endObject();
        }
    
    }
}
