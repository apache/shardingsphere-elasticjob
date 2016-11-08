/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.executor;

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

import java.util.Map;

/**
 * 内部的作业配置上下文.
 *
 * @author caohao
 */
public class JobConfigurationContext implements JobRootConfiguration {
    
    private static final String IGNORE_CRON = "ignoredCron";
    
    private JobTypeConfiguration jobTypeConfig;
    
    @Getter
    private String beanName;
    
    @Getter
    private String applicationContext;
    
    public JobConfigurationContext(final Map<String, String> jobConfigurationMap) {
        int ignoredShardingTotalCount = 1;
        String jobClass = jobConfigurationMap.get("jobClass");
        String jobType = jobConfigurationMap.get("jobType");
        String jobName = jobConfigurationMap.get("jobName");
        String cron = Strings.isNullOrEmpty(jobConfigurationMap.get("cron")) ? IGNORE_CRON : jobConfigurationMap.get("cron");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobType), "jobType can not be empty.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobClass), "jobClass can not be empty.");
        JobCoreConfiguration jobCoreConfig = JobCoreConfiguration.newBuilder(jobName, cron, ignoredShardingTotalCount).build();
        jobCoreConfig.getJobProperties().put(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.name(), jobConfigurationMap.get("executorServiceHandler"));
        jobCoreConfig.getJobProperties().put(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.name(), jobConfigurationMap.get("jobExceptionHandler"));
        if (JobType.DATAFLOW.name().equals(jobType)) {
            jobTypeConfig = new DataflowJobConfiguration(jobCoreConfig, jobClass, Boolean.valueOf(jobConfigurationMap.get("streamingProcess")));
        } else if (JobType.SIMPLE.name().equals(jobType)) {
            jobTypeConfig = new SimpleJobConfiguration(jobCoreConfig, jobClass);
        } else if (JobType.SCRIPT.name().equals(jobType)) {
            jobTypeConfig = new ScriptJobConfiguration(jobCoreConfig, jobConfigurationMap.get("scriptCommandLine"));
        }
        beanName = jobConfigurationMap.get("beanName");
        applicationContext = jobConfigurationMap.get("applicationContext");
    }
    
    /**
     * 判断是否为瞬时作业.
     * 
     * @return 是否为瞬时作业
     */
    public boolean isTransient() {
        return IGNORE_CRON.equals(jobTypeConfig.getCoreConfig().getCron());
    }
    
    @Override
    public JobTypeConfiguration getTypeConfig() {
        return jobTypeConfig;
    }
}
