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

package com.dangdang.ddframe.job.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import com.dangdang.ddframe.job.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.handler.ExecutorServiceHandler;
import com.dangdang.ddframe.job.executor.handler.ExecutorServiceHandlerRegistry;
import com.dangdang.ddframe.job.executor.handler.JobExceptionHandler;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 弹性化分布式作业执行器.
 *
 * @author zhangliang
 */
public abstract class AbstractElasticJobExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final JobFacade jobFacade;
    
    @Getter(AccessLevel.PROTECTED)
    private final JobRootConfiguration jobRootConfig;
    
    private final String jobName;
    
    private final ExecutorService executorService;
    
    private final JobExceptionHandler jobExceptionHandler;
    
    private final JobEventBus jobEventBus = JobEventBus.getInstance();
    
    protected AbstractElasticJobExecutor(final JobFacade jobFacade) {
        this.jobFacade = jobFacade;
        jobRootConfig = jobFacade.loadJobRootConfiguration(true);
        jobName = jobRootConfig.getTypeConfig().getCoreConfig().getJobName();
        executorService = ExecutorServiceHandlerRegistry.getExecutorServiceHandler(jobName, (ExecutorServiceHandler) getHandler(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER));
        jobExceptionHandler = (JobExceptionHandler) getHandler(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER);
        jobEventBus.register(jobName, jobRootConfig.getTypeConfig().getCoreConfig().getJobEventConfigs().values());
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
        ShardingContexts shardingContexts = jobFacade.getShardingContexts();
        if (jobFacade.misfireIfNecessary(shardingContexts.getShardingItemParameters().keySet())) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.DEBUG, "Previous job is still running, misfired job will start after previous job completed."));
            return;
        }
        jobFacade.cleanPreviousExecutionInfo();
        try {
            jobFacade.beforeJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(jobName, cause);
        }
        execute(shardingContexts, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER);
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Execute normal completed, sharding context:%s.", shardingContexts)));
        while (jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Execute misfired job, sharding context:%s.", shardingContexts)));
            jobFacade.clearMisfire(shardingContexts.getShardingItemParameters().keySet());
            execute(shardingContexts, JobExecutionEvent.ExecutionSource.MISFIRE);
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Misfired job completed, sharding context:%s.", shardingContexts)));
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobExceptionHandler.handleException(jobName, cause);
        }
        jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, "Job execute completed."));
    }
    
    private void execute(final ShardingContexts shardingContexts, final JobExecutionEvent.ExecutionSource executionSource) {
        if (shardingContexts.getShardingItemParameters().isEmpty()) {
            jobEventBus.post(new JobTraceEvent(jobName, LogLevel.TRACE, String.format("Sharding item is empty, job execution context: '%s'.", shardingContexts)));
            return;
        }
        jobFacade.registerJobBegin(shardingContexts);
        try {
            process(shardingContexts, executionSource);
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            jobFacade.registerJobCompleted(shardingContexts);
        }
    }
    
    private void process(final ShardingContexts shardingContexts, final JobExecutionEvent.ExecutionSource executionSource) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(jobName, executionSource, item);
            process(shardingContexts, item, jobExecutionEvent);
            return;
        }
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            final JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(jobName, executionSource, each);
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        process(shardingContexts, each, jobExecutionEvent);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void process(final ShardingContexts shardingContexts, final int item, final JobExecutionEvent jobExecutionEvent) {
        jobEventBus.post(jobExecutionEvent);
        jobEventBus.post(new JobTraceEvent(jobName, JobTraceEvent.LogLevel.TRACE, String.format("Job executing, item is: '%s'.", item)));
        try {
            process(new ShardingContext(shardingContexts, item));
            jobExecutionEvent.executionSuccess();
            jobEventBus.post(new JobTraceEvent(jobName, JobTraceEvent.LogLevel.TRACE, String.format("Job executed, item is: '%s'.", item)));
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            jobExecutionEvent.executionFailure(ex);
            jobExceptionHandler.handleException(jobName, ex);
        } finally {
            jobEventBus.post(jobExecutionEvent);
        }
    }
    
    protected abstract void process(ShardingContext shardingContext);
}
