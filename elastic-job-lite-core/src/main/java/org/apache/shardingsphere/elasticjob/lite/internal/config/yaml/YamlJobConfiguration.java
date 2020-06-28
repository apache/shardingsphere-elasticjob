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

package org.apache.shardingsphere.elasticjob.lite.internal.config.yaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;

import java.util.Properties;

/**
 * YAML job configuration.
 */
@Getter
@Setter
public final class YamlJobConfiguration {
    
    private String jobName;
    
    private JobType jobType;
    
    private String cron;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds;
    
    private int reconcileIntervalMinutes;
    
    private int monitorPort;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorServiceHandlerType;
    
    private String jobErrorHandlerType;
    
    private String description;
    
    private Properties props = new Properties();
    
    private boolean disabled;
    
    private boolean overwrite;
    
    /**
     * Convert to job configuration.
     * 
     * @return job configuration
     */
    public JobConfiguration toJobConfiguration() {
        JobConfiguration result = JobConfiguration.newBuilder(jobName, jobType, cron, shardingTotalCount)
                .shardingItemParameters(shardingItemParameters).jobParameter(jobParameter)
                .monitorExecution(monitorExecution).failover(failover).misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds).reconcileIntervalMinutes(reconcileIntervalMinutes).monitorPort(monitorPort)
                .jobShardingStrategyType(jobShardingStrategyType).jobExecutorServiceHandlerType(jobExecutorServiceHandlerType).jobErrorHandlerType(jobErrorHandlerType)
                .description(description).disabled(disabled).overwrite(overwrite).build();
        for (Object each : props.keySet()) {
            result.getProps().setProperty(each.toString(), props.get(each.toString()).toString());
        }
        return result;
    }
    
    /**
     * Convert from job configuration.
     * 
     * @param jobConfiguration job configuration
     * @return YAML job configuration
     */
    public static YamlJobConfiguration fromJobConfiguration(final JobConfiguration jobConfiguration) {
        YamlJobConfiguration result = new YamlJobConfiguration();
        result.setJobName(jobConfiguration.getJobName());
        result.setJobType(jobConfiguration.getJobType());
        result.setCron(jobConfiguration.getCron());
        result.setShardingTotalCount(jobConfiguration.getShardingTotalCount());
        result.setShardingItemParameters(jobConfiguration.getShardingItemParameters());
        result.setJobParameter(jobConfiguration.getJobParameter());
        result.setMonitorExecution(jobConfiguration.isMonitorExecution());
        result.setFailover(jobConfiguration.isFailover());
        result.setMisfire(jobConfiguration.isMisfire());
        result.setMaxTimeDiffSeconds(jobConfiguration.getMaxTimeDiffSeconds());
        result.setReconcileIntervalMinutes(jobConfiguration.getReconcileIntervalMinutes());
        result.setJobShardingStrategyType(jobConfiguration.getJobShardingStrategyType());
        result.setJobExecutorServiceHandlerType(jobConfiguration.getJobExecutorServiceHandlerType());
        result.setJobErrorHandlerType(jobConfiguration.getJobErrorHandlerType());
        result.setDescription(jobConfiguration.getDescription());
        result.setProps(jobConfiguration.getProps());
        result.setDisabled(jobConfiguration.isDisabled());
        result.setOverwrite(jobConfiguration.isOverwrite());
        return result;
    }
}
