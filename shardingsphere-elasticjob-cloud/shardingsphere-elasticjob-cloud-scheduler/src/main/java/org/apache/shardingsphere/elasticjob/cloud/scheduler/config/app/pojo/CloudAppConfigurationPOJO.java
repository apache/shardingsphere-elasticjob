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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.pojo;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;

/**
 * Cloud app configuration POJO.
 */
@Getter
@Setter
public final class CloudAppConfigurationPOJO {
    
    private String appName;
    
    private String appURL;
    
    private String bootstrapScript;
    
    private double cpuCount = 1d;
    
    private double memoryMB = 128d;
    
    private boolean appCacheEnable = true;
    
    private int eventTraceSamplingCount;
    
    /**
     * Convert to cloud app configuration.
     *
     * @return cloud app configuration
     */
    public CloudAppConfiguration toCloudAppConfiguration() {
        return new CloudAppConfiguration(appName, appURL, bootstrapScript, cpuCount, memoryMB, appCacheEnable, eventTraceSamplingCount);
    }
    
    /**
     * Convert from cloud app configuration.
     *
     * @param cloudAppConfig cloud job configuration
     * @return cloud app configuration POJO
     */
    public static CloudAppConfigurationPOJO fromCloudAppConfiguration(final CloudAppConfiguration cloudAppConfig) {
        CloudAppConfigurationPOJO result = new CloudAppConfigurationPOJO();
        result.setAppName(cloudAppConfig.getAppName());
        result.setAppURL(cloudAppConfig.getAppURL());
        result.setBootstrapScript(cloudAppConfig.getBootstrapScript());
        result.setCpuCount(cloudAppConfig.getCpuCount());
        result.setMemoryMB(cloudAppConfig.getMemoryMB());
        result.setAppCacheEnable(cloudAppConfig.isAppCacheEnable());
        result.setEventTraceSamplingCount(cloudAppConfig.getEventTraceSamplingCount());
        return result;
    }
}
