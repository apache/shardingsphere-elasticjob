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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.cloud.api.JobType;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.JobProperties.JobPropertiesEnum;

import java.util.Map;

/**
 * Job configuration context.
 */
@Getter
public final class JobConfigurationContext {
    
    private final JobTypeConfiguration typeConfig;
    
    private final String beanName;
    
    private final String applicationContext;
    
    private final boolean isTransient;
    
    public JobConfigurationContext(final Map<String, String> jobConfigurationMap) {
        typeConfig = getJobTypeConfiguration(jobConfigurationMap);
        beanName = jobConfigurationMap.get("beanName");
        applicationContext = jobConfigurationMap.get("applicationContext");
        isTransient = Strings.isNullOrEmpty(typeConfig.getCoreConfig().getCron());
    }
    
    private JobTypeConfiguration getJobTypeConfiguration(final Map<String, String> jobConfigurationMap) {
        int ignoredShardingTotalCount = 1;
        String jobClass = jobConfigurationMap.get("jobClass");
        String jobType = jobConfigurationMap.get("jobType");
        String jobName = jobConfigurationMap.get("jobName");
        String cron = jobConfigurationMap.get("cron");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobType), "jobType can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass can not be empty.");
        JobCoreConfiguration jobCoreConfig = JobCoreConfiguration.newBuilder(jobName, cron, ignoredShardingTotalCount).build();
        jobCoreConfig.getJobProperties().put(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.name(), jobConfigurationMap.get("executorServiceHandler"));
        jobCoreConfig.getJobProperties().put(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.name(), jobConfigurationMap.get("jobExceptionHandler"));
        if (JobType.DATAFLOW.name().equals(jobType)) {
            return new DataflowJobConfiguration(jobCoreConfig, jobClass, Boolean.valueOf(jobConfigurationMap.get("streamingProcess")));
        } else if (JobType.SCRIPT.name().equals(jobType)) {
            return new ScriptJobConfiguration(jobCoreConfig, jobConfigurationMap.get("scriptCommandLine"));
        }
        return new SimpleJobConfiguration(jobCoreConfig, jobClass);
    }
}
