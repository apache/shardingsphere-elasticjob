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

package org.apache.shardingsphere.elasticjob.cloud.util.json;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;

/**
 * Cloud job configuration gson factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudJobConfigurationGsonFactory {
    
    static {
        GsonFactory.registerTypeAdapter(CloudJobConfiguration.class, new JobConfigurationGsonTypeAdapter());
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
}
