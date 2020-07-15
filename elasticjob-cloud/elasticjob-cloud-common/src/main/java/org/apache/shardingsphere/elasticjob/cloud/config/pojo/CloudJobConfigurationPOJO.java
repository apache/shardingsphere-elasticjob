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

package org.apache.shardingsphere.elasticjob.cloud.config.pojo;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;

import java.util.Properties;

/**
 * Cloud job configuration POJO.
 */
@Getter
@Setter
public final class CloudJobConfigurationPOJO {
    
    private String appName;
    
    private double cpuCount;
    
    private double memoryMB;
    
    private CloudJobExecutionType jobExecutionType;
    
    private String jobName;
    
    private String cron;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds;
    
    private int reconcileIntervalMinutes;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorServiceHandlerType;
    
    private String jobErrorHandlerType;
    
    private String description;
    
    private Properties props = new Properties();
    
    private boolean disabled;
    
    private boolean overwrite;
    
    /**
     * Convert to cloud job configuration.
     *
     * @return cloud job configuration
     */
    public CloudJobConfiguration toCloudJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder(jobName, shardingTotalCount)
                .cron(cron).shardingItemParameters(shardingItemParameters).jobParameter(jobParameter)
                .monitorExecution(monitorExecution).failover(failover).misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds).reconcileIntervalMinutes(reconcileIntervalMinutes)
                .jobShardingStrategyType(jobShardingStrategyType).jobExecutorServiceHandlerType(jobExecutorServiceHandlerType).jobErrorHandlerType(jobErrorHandlerType)
                .description(description).disabled(disabled).overwrite(overwrite).build();
        for (Object each : props.keySet()) {
            jobConfig.getProps().setProperty(each.toString(), props.get(each.toString()).toString());
        }
        return new CloudJobConfiguration(appName, cpuCount, memoryMB, jobExecutionType, jobConfig);
    }
    
    /**
     * Convert from cloud job configuration.
     *
     * @param cloudJobConfig cloud job configuration
     * @return cloud job configuration POJO
     */
    public static CloudJobConfigurationPOJO fromCloudJobConfiguration(final CloudJobConfiguration cloudJobConfig) {
        CloudJobConfigurationPOJO result = new CloudJobConfigurationPOJO();
        result.setAppName(cloudJobConfig.getAppName());
        result.setCpuCount(cloudJobConfig.getCpuCount());
        result.setMemoryMB(cloudJobConfig.getMemoryMB());
        result.setJobExecutionType(cloudJobConfig.getJobExecutionType());
        result.setJobName(cloudJobConfig.getJobConfig().getJobName());
        result.setCron(cloudJobConfig.getJobConfig().getCron());
        result.setShardingTotalCount(cloudJobConfig.getJobConfig().getShardingTotalCount());
        result.setShardingItemParameters(cloudJobConfig.getJobConfig().getShardingItemParameters());
        result.setJobParameter(cloudJobConfig.getJobConfig().getJobParameter());
        result.setMonitorExecution(cloudJobConfig.getJobConfig().isMonitorExecution());
        result.setFailover(cloudJobConfig.getJobConfig().isFailover());
        result.setMisfire(cloudJobConfig.getJobConfig().isMisfire());
        result.setMaxTimeDiffSeconds(cloudJobConfig.getJobConfig().getMaxTimeDiffSeconds());
        result.setReconcileIntervalMinutes(cloudJobConfig.getJobConfig().getReconcileIntervalMinutes());
        result.setJobShardingStrategyType(cloudJobConfig.getJobConfig().getJobShardingStrategyType());
        result.setJobExecutorServiceHandlerType(cloudJobConfig.getJobConfig().getJobExecutorServiceHandlerType());
        result.setJobErrorHandlerType(cloudJobConfig.getJobConfig().getJobErrorHandlerType());
        result.setDescription(cloudJobConfig.getJobConfig().getDescription());
        result.setProps(cloudJobConfig.getJobConfig().getProps());
        result.setDisabled(cloudJobConfig.getJobConfig().isDisabled());
        result.setOverwrite(cloudJobConfig.getJobConfig().isOverwrite());
        return result;
    }
}
