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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.constants.CloudConfigurationConstants;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.util.json.AbstractJobConfigurationGsonTypeAdapter;
import org.apache.shardingsphere.elasticjob.cloud.util.json.GsonFactory;
import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Map;

/**
 * Cloud job configuration gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new CloudJobConfigurationGsonTypeAdapter());
    }
    
    /**
     * Convert cloud job configuration to json string.
     *
     * @param cloudJobConfig cloud job configuration
     * @return the json string of the cloud job configuration
     */
    public static String toJson(final CloudJobConfiguration cloudJobConfig) {
        return GsonFactory.getGson().toJson(cloudJobConfig);
    }
    
    /**
     * Convert json string to cloud job configuration.
     *
     * @param cloudJobConfigJson json string of the cloud job configuration
     * @return cloud job configuration
     */
    public static CloudJobConfiguration fromJson(final String cloudJobConfigJson) {
        return GsonFactory.getGson().fromJson(cloudJobConfigJson, CloudJobConfiguration.class);
    }

    /**
     * Json adapter of the cloud job configuration.
     */
    public static final class CloudJobConfigurationGsonTypeAdapter extends AbstractJobConfigurationGsonTypeAdapter<CloudJobConfiguration> {
        
        @Override
        protected void addToCustomizedValueMap(final String jsonName, final JsonReader in, final Map<String, Object> customizedValueMap) throws IOException {
            switch (jsonName) {
                case CloudConfigurationConstants.CPU_COUNT:
                case CloudConfigurationConstants.MEMORY_MB:
                    customizedValueMap.put(jsonName, in.nextDouble());
                    break;
                case CloudConfigurationConstants.APP_NAME:
                case CloudConfigurationConstants.APPLICATION_CONTEXT:
                case CloudConfigurationConstants.BEAN_NAME:
                case CloudConfigurationConstants.JOB_EXECUTION_TYPE:
                    customizedValueMap.put(jsonName, in.nextString());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        
        @Override
        protected CloudJobConfiguration getJobRootConfiguration(final JobTypeConfiguration typeConfig, final Map<String, Object> customizedValueMap) {
            Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.APP_NAME), "appName cannot be null.");
            Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT), "cpuCount cannot be null.");
            Preconditions.checkArgument((double) customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT) >= 0.001, "cpuCount cannot be less than 0.001");
            Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB), "memoryMB cannot be null.");
            Preconditions.checkArgument((double) customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB) >= 1, "memory cannot be less than 1");
            Preconditions.checkNotNull(customizedValueMap.get(CloudConfigurationConstants.JOB_EXECUTION_TYPE), "jobExecutionType cannot be null.");
            if (customizedValueMap.containsKey(CloudConfigurationConstants.BEAN_NAME) && customizedValueMap.containsKey(CloudConfigurationConstants.APPLICATION_CONTEXT)) {
                return new CloudJobConfiguration((String) customizedValueMap.get(CloudConfigurationConstants.APP_NAME), typeConfig,
                        (double) customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT),
                        (double) customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB),
                        CloudJobExecutionType.valueOf(customizedValueMap.get(CloudConfigurationConstants.JOB_EXECUTION_TYPE).toString()),
                        customizedValueMap.get(CloudConfigurationConstants.BEAN_NAME).toString(), customizedValueMap.get(CloudConfigurationConstants.APPLICATION_CONTEXT).toString());
            } else {
                return new CloudJobConfiguration((String) customizedValueMap.get(CloudConfigurationConstants.APP_NAME), typeConfig,
                        (double) customizedValueMap.get(CloudConfigurationConstants.CPU_COUNT),
                        (double) customizedValueMap.get(CloudConfigurationConstants.MEMORY_MB),
                        CloudJobExecutionType.valueOf(customizedValueMap.get(CloudConfigurationConstants.JOB_EXECUTION_TYPE).toString()));
            }
        }
        
        @Override
        protected void writeCustomized(final JsonWriter out, final CloudJobConfiguration value) throws IOException {
            out.name(CloudConfigurationConstants.APP_NAME).value(value.getAppName());
            out.name(CloudConfigurationConstants.CPU_COUNT).value(value.getCpuCount());
            out.name(CloudConfigurationConstants.MEMORY_MB).value(value.getMemoryMB());
            out.name(CloudConfigurationConstants.JOB_EXECUTION_TYPE).value(value.getJobExecutionType().name());
            out.name(CloudConfigurationConstants.BEAN_NAME).value(value.getBeanName());
            out.name(CloudConfigurationConstants.APPLICATION_CONTEXT).value(value.getApplicationContext());
        }
    }
}
