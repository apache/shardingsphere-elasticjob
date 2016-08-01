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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.common;

import com.dangdang.ddframe.job.api.config.JobCoreConfiguration.Builder;
import com.dangdang.ddframe.job.api.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.api.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 基本作业配置命名空间对象.
 *
 * @author caohao
 * @author zhangliang
 */
@Setter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractJobConfigurationDto {
    
    private final String jobName;
    
    private final String cron;
    
    private final int shardingTotalCount;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private Boolean failover;
    
    private Boolean misfire;
    
    private String description;
    
    private Boolean monitorExecution;
    
    private Integer maxTimeDiffSeconds;
    
    private Integer monitorPort;
    
    private String jobShardingStrategyClass;
    
    private Boolean disabled;
    
    private Boolean overwrite;
    
    private String executorServiceHandler;
    
    private String jobExceptionHandler;
    
    public LiteJobConfiguration toLiteJobConfiguration() {
        JobCoreConfiguration jobCoreConfig = buildJobCoreConfiguration();
        return buildLiteJobConfiguration(jobCoreConfig);
    }
    
    private JobCoreConfiguration buildJobCoreConfiguration() {
        Builder jobCoreConfigBuilder = JobCoreConfiguration.newBuilder(jobName, cron, shardingTotalCount);
        jobCoreConfigBuilder.shardingItemParameters(shardingItemParameters);
        jobCoreConfigBuilder.jobParameter(jobParameter);
        if (null != failover) {
            jobCoreConfigBuilder.failover(failover);
        }
        if (null != misfire) {
            jobCoreConfigBuilder.misfire(misfire);
        }
        if (null != executorServiceHandler) {
            try {
                jobCoreConfigBuilder.jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.name(), Class.forName(executorServiceHandler));
            } catch (final ClassNotFoundException ex) {
                log.warn("Cannot load executor service handler '{}', use default {} class.", executorServiceHandler, JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.name());
            }
        }
        if (null != jobExceptionHandler) {
            try {
                jobCoreConfigBuilder.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.name(), Class.forName(jobExceptionHandler));
            } catch (final ClassNotFoundException ex) {
                log.warn("Cannot load job exception handler '{}', use default {} class.", jobExceptionHandler, JobPropertiesEnum.JOB_EXCEPTION_HANDLER.name());
            }
        }
        jobCoreConfigBuilder.description(description);
        return jobCoreConfigBuilder.build();
    }
    
    private LiteJobConfiguration buildLiteJobConfiguration(final JobCoreConfiguration jobCoreConfig) {
        LiteJobConfiguration.Builder result = LiteJobConfiguration.newBuilder(toJobConfiguration(jobCoreConfig));
        if (null != monitorExecution) {
            result.monitorExecution(monitorExecution);
        }
        if (null != maxTimeDiffSeconds) {
            result.maxTimeDiffSeconds(maxTimeDiffSeconds);
        }
        if (null != monitorPort) {
            result.monitorPort(monitorPort);
        }
        result.jobShardingStrategyClass(jobShardingStrategyClass);
        if (null != disabled) {
            result.disabled(disabled);
        }
        if (null != overwrite) {
            result.overwrite(overwrite);
        }
        if (null != overwrite) {
            result.overwrite(overwrite);
        }
        return result.build();
    }
    
    protected abstract JobTypeConfiguration toJobConfiguration(final JobCoreConfiguration jobCoreConfig);
}
