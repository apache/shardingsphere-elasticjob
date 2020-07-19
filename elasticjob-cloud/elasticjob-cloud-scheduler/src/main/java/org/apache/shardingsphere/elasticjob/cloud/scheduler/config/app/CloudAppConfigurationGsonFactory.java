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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.util.json.CloudConfigurationConstants;
import org.apache.shardingsphere.elasticjob.cloud.util.json.GsonFactory;

/**
 * Cloud app configuration gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudAppConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(CloudAppConfiguration.class, new CloudAppConfigurationGsonTypeAdapter());
    }
    
    /**
     * Convert cloud app configuration to json string.
     * @param cloudAppConfig cloud app config
     * @return json string
     */
    public static String toJson(final CloudAppConfiguration cloudAppConfig) {
        return GsonFactory.getGson().toJson(cloudAppConfig);
    }
    
    /**
     * Convert json string to cloud app configuration.
     * @param cloudAppConfigJson json string of the cloud app configuration
     * @return cloud app configuration
     */
    public static CloudAppConfiguration fromJson(final String cloudAppConfigJson) {
        return GsonFactory.getGson().fromJson(cloudAppConfigJson, CloudAppConfiguration.class);
    }
    
    /**
     * Json adapter of the cloud app configuration.
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
                    case CloudConfigurationConstants.APP_NAME:
                        appName = in.nextString();
                        break;
                    case CloudConfigurationConstants.APP_URL:
                        appURL = in.nextString();
                        break;
                    case CloudConfigurationConstants.BOOTSTRAP_SCRIPT:
                        bootstrapScript = in.nextString();
                        break;
                    case CloudConfigurationConstants.CPU_COUNT:
                        cpuCount = in.nextDouble();
                        break;
                    case CloudConfigurationConstants.MEMORY_MB:
                        memoryMB = in.nextDouble();
                        break;
                    case CloudConfigurationConstants.APP_CACHE_ENABLE:
                        appCacheEnable = in.nextBoolean();
                        break;
                    case CloudConfigurationConstants.EVENT_TRACE_SAMPLING_COUNT:
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
            out.name(CloudConfigurationConstants.APP_NAME).value(value.getAppName());
            out.name(CloudConfigurationConstants.APP_URL).value(value.getAppURL());
            out.name(CloudConfigurationConstants.BOOTSTRAP_SCRIPT).value(value.getBootstrapScript());
            out.name(CloudConfigurationConstants.CPU_COUNT).value(value.getCpuCount());
            out.name(CloudConfigurationConstants.MEMORY_MB).value(value.getMemoryMB());
            out.name(CloudConfigurationConstants.APP_CACHE_ENABLE).value(value.isAppCacheEnable());
            out.name(CloudConfigurationConstants.EVENT_TRACE_SAMPLING_COUNT).value(value.getEventTraceSamplingCount());
            out.endObject();
        }
    }
}
