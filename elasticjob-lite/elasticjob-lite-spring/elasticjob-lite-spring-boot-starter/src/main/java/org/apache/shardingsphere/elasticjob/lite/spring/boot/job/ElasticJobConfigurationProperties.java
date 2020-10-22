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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * ElasticJob configuration properties.
 */
@Getter
@Setter
public final class ElasticJobConfigurationProperties {
    
    private Class<? extends ElasticJob> elasticJobClass;
    
    private String elasticJobType;
    
    private String cron;
    
    private String jobBootstrapBeanName;
    
    private int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private boolean monitorExecution;
    
    private boolean failover;
    
    private boolean misfire;
    
    private int maxTimeDiffSeconds = -1;
    
    private int reconcileIntervalMinutes;
    
    private String jobShardingStrategyType;
    
    private String jobExecutorServiceHandlerType;
    
    private String jobErrorHandlerType;
    
    private Collection<String> jobListenerTypes = new LinkedList<>();
    
    private String description;
    
    private Properties props = new Properties();
    
    private boolean disabled;
    
    private boolean overwrite;
    
    /**
     * Convert to job configuration.
     *
     * @param jobName job name
     * @return job configuration
     */
    public JobConfiguration toJobConfiguration(final String jobName) {
        JobConfiguration result = JobConfiguration.newBuilder(jobName, shardingTotalCount)
                .cron(cron).shardingItemParameters(shardingItemParameters).jobParameter(jobParameter)
                .monitorExecution(monitorExecution).failover(failover).misfire(misfire)
                .maxTimeDiffSeconds(maxTimeDiffSeconds).reconcileIntervalMinutes(reconcileIntervalMinutes)
                .jobShardingStrategyType(jobShardingStrategyType).jobExecutorServiceHandlerType(jobExecutorServiceHandlerType).jobErrorHandlerType(jobErrorHandlerType)
                .jobListenerTypes(jobListenerTypes.toArray(new String[0])).description(description).disabled(disabled).overwrite(overwrite).build();
        props.stringPropertyNames().forEach(each -> result.getProps().setProperty(each, props.getProperty(each)));
        return result;
    }
}
