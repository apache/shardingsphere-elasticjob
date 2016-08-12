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

package com.dangdang.ddframe.job.api.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.config.JobRootConfiguration;
import com.dangdang.ddframe.job.api.config.impl.JobProperties;
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.exception.JobSystemException;
import com.dangdang.ddframe.job.api.executor.handler.ExecutorServiceHandler;
import com.dangdang.ddframe.job.api.executor.handler.JobExceptionHandler;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.ExecutorService;

/**
 * 弹性化分布式作业执行器.
 *
 * @author zhangliang
 */
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractElasticJobExecutor {
    
    private final JobFacade jobFacade;
    
    private final JobRootConfiguration jobRootConfig;
    
    private final String jobName;
    
    private final ExecutorService executorService;
    
    private final JobExceptionHandler jobExceptionHandler;
    
    private final JobEventBus jobEventBus = JobEventBus.getInstance();
    
    protected AbstractElasticJobExecutor(final JobFacade jobFacade) {
        this.jobFacade = jobFacade;
        jobRootConfig = jobFacade.loadJobRootConfiguration(true);
        jobName = jobRootConfig.getTypeConfig().getCoreConfig().getJobName();
        executorService = ((ExecutorServiceHandler) getHandler(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER)).createExecutorService();
        jobExceptionHandler = (JobExceptionHandler) getHandler(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER);
        jobEventBus.register(jobRootConfig.getTypeConfig().getCoreConfig().getJobEventConfigs());
    }
    
    private Object getHandler(final JobProperties.JobPropertiesEnum jobPropertiesEnum) {
        String handlerClassName = jobRootConfig.getTypeConfig().getCoreConfig().getJobProperties().get(jobPropertiesEnum);
        try {
            Class<?> handlerClass = Class.forName(handlerClassName);
            if (jobPropertiesEnum.getClassType().isAssignableFrom(handlerClass)) {
                return handlerClass.newInstance();
            }
            return getDefaultHandler(jobPropertiesEnum, handlerClassName);
        } catch (final ReflectiveOperationException ex) {
            return getDefaultHandler(jobPropertiesEnum, handlerClassName);
        }
    }
    
    private Object getDefaultHandler(final JobProperties.JobPropertiesEnum jobPropertiesEnum, final String handlerClassName) {
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.WARN,
                String.format("Cannot instantiation class '%s', use default %s class.", handlerClassName, jobPropertiesEnum.getKey())));
        try {
            return Class.forName(jobPropertiesEnum.getDefaultValue()).newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new JobSystemException(e);
        }
    }
    
    /**
     * 执行作业.
     */
    public final void execute() {
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, "Job execute begin."));
        try {
            jobFacade.checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException cause) {
            jobExceptionHandler.handleException(jobName, cause);
        }
        
        ShardingContext shardingContext = jobFacade.getShardingContext();
        if (jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.DEBUG, "Previous job is still running, misfired job will start after previous job completed."));
            return;
        }
        jobFacade.cleanPreviousExecutionInfo();
        try {
            jobFacade.beforeJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(jobName, cause);
        }
        execute(shardingContext, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER);
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Execute normal completed, sharding context:%s.", shardingContext)));
        while (jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Execute misfired job, sharding context:%s.", shardingContext)));
            jobFacade.clearMisfire(shardingContext.getShardingItemParameters().keySet());
            execute(shardingContext, JobExecutionEvent.ExecutionSource.MISFIRE);
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Misfired job completed, sharding context:%s.", shardingContext)));
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(jobName, cause);
        }
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, "Execute all completed."));
    }
    
    private void execute(final ShardingContext shardingContext, final JobExecutionEvent.ExecutionSource executionSource) {
        if (shardingContext.getShardingItemParameters().isEmpty()) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Sharding item is empty, job execution context:%s.", shardingContext)));
            return;
        }
        jobFacade.registerJobBegin(shardingContext);
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(jobName, executionSource, shardingContext.getShardingItemParameters().keySet());
        try {
            jobEventBus.post(jobExecutionEvent);
            process(shardingContext);
            jobExecutionEvent.executionSuccess();
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExecutionEvent.executionFailure(cause);
            jobExceptionHandler.handleException(jobName, cause);
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            jobFacade.registerJobCompleted(shardingContext);
            jobEventBus.post(jobExecutionEvent);
        }
    }
    
    protected abstract void process(final ShardingContext shardingContext);
}
