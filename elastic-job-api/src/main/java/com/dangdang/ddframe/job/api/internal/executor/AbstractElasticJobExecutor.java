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

package com.dangdang.ddframe.job.api.internal.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobProperties;
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.exception.JobSystemException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 弹性化分布式作业执行器.
 * 
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractElasticJobExecutor {
    
    private final JobFacade jobFacade;
    
    private final JobConfiguration jobConfig;
    
    private final ExecutorService executorService;
    
    private final JobExceptionHandler jobExceptionHandler;
    
    protected AbstractElasticJobExecutor(final JobFacade jobFacade) {
        this.jobFacade = jobFacade;
        jobConfig = jobFacade.loadJobConfiguration(true);
        executorService = ((ExecutorServiceHandler) getHandler(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER)).createExecutorService();
        jobExceptionHandler = (JobExceptionHandler) getHandler(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER);
    }
    
    private Object getHandler(final JobProperties.JobPropertiesEnum jobPropertiesEnum) {
        Class<?> handlerClass = jobConfig.getTypeConfig().getCoreConfig().getJobProperties().get(jobPropertiesEnum);
        try {
            return handlerClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            log.warn("Cannot instantiation class '{}', use default {} class.", handlerClass, jobPropertiesEnum.getKey());
            try {
                return jobPropertiesEnum.getDefaultValue().newInstance();
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new JobSystemException(e);
            }
        }
    }
    
    /**
     * 执行作业.
     */
    public final void execute() {
        log.trace("Elastic job: job execute begin.");
        try {
            jobFacade.checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException cause) {
            jobExceptionHandler.handleException(cause);
        }
        
        ShardingContext shardingContext = jobFacade.getShardingContext();
        if (jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())) {
            log.debug("Elastic job: previous job is still running, new job will start after previous job completed. Misfired job had recorded.");
            return;
        }
        jobFacade.cleanPreviousExecutionInfo();
        try {
            jobFacade.beforeJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(cause);
        }
        execute(shardingContext);
        log.trace("Elastic job: execute normal completed, sharding context:{}.", shardingContext);
        while (jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())) {
            log.trace("Elastic job: execute misfired job, sharding context:{}.", shardingContext);
            jobFacade.clearMisfire(shardingContext.getShardingItemParameters().keySet());
            execute(shardingContext);
            log.trace("Elastic job: misfired job completed, sharding context:{}.", shardingContext);
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(cause);
        }
        log.trace("Elastic job: execute all completed.");
    }
    
    private void execute(final ShardingContext shardingContext) {
        if (shardingContext.getShardingItemParameters().isEmpty()) {
            log.trace("Elastic job: sharding item is empty, job execution context:{}.", shardingContext);
            return;
        }
        jobFacade.registerJobBegin(shardingContext);
        try {
            process(shardingContext);
        //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
        //CHECKSTYLE:ON
            jobExceptionHandler.handleException(cause);
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            jobFacade.registerJobCompleted(shardingContext);
        }
    }
    
    protected abstract void process(final ShardingContext shardingContext);
}
