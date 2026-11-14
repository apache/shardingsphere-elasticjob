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

package org.apache.shardingsphere.elasticjob.spring.namespace.job.parser;

import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.JobExtraConfiguration;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Factory bean of {@link JobConfiguration} defined by the Spring Namespace.
 *
 * <p>Spring AOT cannot process the private all-args constructor of {@link JobConfiguration}, so the namespace sets the
 * properties of this factory bean and builds the configuration with {@link JobConfiguration.Builder}.</p>
 */
@Setter
public final class JobConfigurationFactoryBean implements FactoryBean<JobConfiguration> {
    
    private String jobName;
    
    private String cron;
    
    private String timeZone;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds;
    
    private int reconcileIntervalMinutes;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorThreadPoolSizeProviderType;
    
    private String jobErrorHandlerType;
    
    private Collection<String> jobListenerTypes = new ArrayList<>();
    
    private Collection<JobExtraConfiguration> extraConfigurations = new LinkedList<>();
    
    private String description;
    
    private Properties props = new Properties();
    
    private boolean disabled;
    
    private boolean overwrite;
    
    private String label;
    
    private boolean staticSharding;
    
    private long maxWaitMillis;
    
    @Override
    public JobConfiguration getObject() {
        JobConfiguration.Builder result = JobConfiguration.newBuilder(jobName, shardingTotalCount)
                .cron(cron)
                .timeZone(timeZone)
                .shardingItemParameters(shardingItemParameters)
                .jobParameter(jobParameter)
                .monitorExecution(monitorExecution)
                .failover(failover)
                .misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .reconcileIntervalMinutes(reconcileIntervalMinutes)
                .jobShardingStrategyType(jobShardingStrategyType)
                .jobExecutorThreadPoolSizeProviderType(jobExecutorThreadPoolSizeProviderType)
                .jobErrorHandlerType(jobErrorHandlerType)
                .jobListenerTypes(jobListenerTypes.toArray(new String[0]))
                .description(description)
                .disabled(disabled)
                .overwrite(overwrite)
                .label(label)
                .staticSharding(staticSharding)
                .maxWaitMillis(maxWaitMillis);
        extraConfigurations.forEach(result::addExtraConfigurations);
        for (String each : props.stringPropertyNames()) {
            result.setProperty(each, props.getProperty(each));
        }
        return result.build();
    }
    
    @Override
    public Class<?> getObjectType() {
        return JobConfiguration.class;
    }
}
