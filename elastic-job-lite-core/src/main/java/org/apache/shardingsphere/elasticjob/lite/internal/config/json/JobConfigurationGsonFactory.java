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

package org.apache.shardingsphere.elasticjob.lite.internal.config.json;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.util.json.GsonFactory;

/**
 * Job configuration gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(JobConfiguration.class, new JobConfigurationGsonTypeAdapter());
    }
    
    /**
     * Transform job configuration to json.
     * 
     * @param jobConfig job configuration
     * @return job configuration json string
     */
    public static String toJson(final JobConfiguration jobConfig) {
        return GsonFactory.getGson().toJson(jobConfig);
    }
    
    /**
     * Transform job configuration to json.
     *
     * @param jobConfig job configuration
     * @return job configuration json string
     */
    public static String toJsonForObject(final Object jobConfig) {
        return GsonFactory.getGson().toJson(jobConfig);
    }
    
    /**
     * Transform json string to job configuration.
     *
     * @param jobConfigJson job configuration json string
     * @return job configuration
     */
    public static JobConfiguration fromJson(final String jobConfigJson) {
        return GsonFactory.getGson().fromJson(jobConfigJson, JobConfiguration.class);
    }
}
