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

package org.apache.shardingsphere.elasticjob.kernel.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.apache.shardingsphere.elasticjob.spi.yaml.YamlConfiguration;
import org.apache.shardingsphere.elasticjob.spi.yaml.YamlConfigurationConverter;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Job configuration POJO.
 */
@Getter
@Setter
public final class JobConfigurationPOJO {
    
    private String jobName;
    
    private String cron;
    
    private String timeZone;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds = -1;
    
    private int reconcileIntervalMinutes;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorThreadPoolSizeProviderType;
    
    private String jobErrorHandlerType;
    
    private Collection<String> jobListenerTypes = new ArrayList<>();
    
    private Collection<YamlConfiguration<JobExtraConfiguration>> jobExtraConfigurations = new LinkedList<>();
    
    private String description;
    
    private Properties props = new Properties();
    
    private boolean disabled;
    
    private boolean overwrite;
    
    private String label;
    
    private boolean staticSharding;
    
    /**
     * Convert to job configuration.
     *
     * @return job configuration
     */
    public JobConfiguration toJobConfiguration() {
        JobConfiguration result = JobConfiguration.newBuilder(jobName, shardingTotalCount)
                .cron(cron).timeZone(timeZone).shardingItemParameters(shardingItemParameters).jobParameter(jobParameter)
                .monitorExecution(monitorExecution).failover(failover).misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds).reconcileIntervalMinutes(reconcileIntervalMinutes)
                .jobShardingStrategyType(jobShardingStrategyType).jobExecutorThreadPoolSizeProviderType(jobExecutorThreadPoolSizeProviderType)
                .jobErrorHandlerType(jobErrorHandlerType).jobListenerTypes(jobListenerTypes.toArray(new String[]{})).description(description)
                .disabled(disabled).overwrite(overwrite).label(label).staticSharding(staticSharding).build();
        jobExtraConfigurations.stream().map(YamlConfiguration::toConfiguration).forEach(result.getExtraConfigurations()::add);
        for (Object each : props.keySet()) {
            result.getProps().setProperty(each.toString(), props.get(each.toString()).toString());
        }
        return result;
    }
    
    /**
     * Convert from job configuration.
     *
     * @param jobConfig job configuration
     * @return job configuration POJO
     */
    @SuppressWarnings("unchecked")
    public static JobConfigurationPOJO fromJobConfiguration(final JobConfiguration jobConfig) {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName(jobConfig.getJobName());
        result.setCron(jobConfig.getCron());
        result.setTimeZone(jobConfig.getTimeZone());
        result.setShardingTotalCount(jobConfig.getShardingTotalCount());
        result.setShardingItemParameters(jobConfig.getShardingItemParameters());
        result.setJobParameter(jobConfig.getJobParameter());
        result.setMonitorExecution(jobConfig.isMonitorExecution());
        result.setFailover(jobConfig.isFailover());
        result.setMisfire(jobConfig.isMisfire());
        result.setMaxTimeDiffSeconds(jobConfig.getMaxTimeDiffSeconds());
        result.setReconcileIntervalMinutes(jobConfig.getReconcileIntervalMinutes());
        result.setJobShardingStrategyType(jobConfig.getJobShardingStrategyType());
        result.setJobExecutorThreadPoolSizeProviderType(jobConfig.getJobExecutorThreadPoolSizeProviderType());
        result.setJobErrorHandlerType(jobConfig.getJobErrorHandlerType());
        result.setJobListenerTypes(jobConfig.getJobListenerTypes());
        jobConfig.getExtraConfigurations().stream()
                .map(each -> TypedSPILoader.getService(YamlConfigurationConverter.class, each.getClass()).convertToYamlConfiguration(each)).forEach(result.getJobExtraConfigurations()::add);
        result.setDescription(jobConfig.getDescription());
        result.setProps(jobConfig.getProps());
        result.setDisabled(jobConfig.isDisabled());
        result.setOverwrite(jobConfig.isOverwrite());
        result.setLabel(jobConfig.getLabel());
        result.setStaticSharding(jobConfig.isStaticSharding());
        return result;
    }
}
