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

package io.elasticjob.lite.executor.handler;

import io.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import io.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;
import io.elasticjob.lite.util.json.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Job properties.
 */
@AllArgsConstructor
@NoArgsConstructor
public final class JobProperties {
    
    private EnumMap<JobPropertiesEnum, String> map = new EnumMap<>(JobPropertiesEnum.class);
    
    /**
     * Put job property.
     * 
     * @param key property key
     * @param value property value
     */
    public void put(final String key, final String value) {
        JobPropertiesEnum jobPropertiesEnum = JobPropertiesEnum.from(key);
        if (null == jobPropertiesEnum || null == value) {
            return;
        }
        map.put(jobPropertiesEnum, value);
    }
    
    /**
     * Get job property.
     * 
     * @param jobPropertiesEnum job properties enum
     * @return property value
     */
    public String get(final JobPropertiesEnum jobPropertiesEnum) {
        return map.containsKey(jobPropertiesEnum) ? map.get(jobPropertiesEnum) : jobPropertiesEnum.getDefaultValue();
    }
    
    /**
     * Get all keys.
     * 
     * @return all keys
     */
    public String json() {
        Map<String, String> jsonMap = new LinkedHashMap<>(JobPropertiesEnum.values().length, 1);
        for (JobPropertiesEnum each : JobPropertiesEnum.values()) {
            jsonMap.put(each.getKey(), get(each));
        }
        return GsonFactory.getGson().toJson(jsonMap);
    }
    
    /**
     * Job properties enum.
     */
    @RequiredArgsConstructor
    @Getter
    public enum JobPropertiesEnum {
        
        /**
         * Job execution handler.
         */
        JOB_EXCEPTION_HANDLER("job_exception_handler", JobExceptionHandler.class, DefaultJobExceptionHandler.class.getCanonicalName()),
        
        /**
         * Executor service handler.
         */
        EXECUTOR_SERVICE_HANDLER("executor_service_handler", ExecutorServiceHandler.class, DefaultExecutorServiceHandler.class.getCanonicalName());
        
        private final String key;
    
        private final Class<?> classType;
        
        private final String defaultValue;
        
        /**
         * Get job properties enum via key.
         * 
         * @param key property key
         * @return job properties enum
         */
        public static JobPropertiesEnum from(final String key) {
            for (JobPropertiesEnum each : JobPropertiesEnum.values()) {
                if (each.getKey().equals(key)) {
                    return each;
                }
            }
            return null;
        }
    }
}
