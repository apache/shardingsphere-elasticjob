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

package org.apache.shardingsphere.elasticjob.lite.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.simple.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.ExceptionUtils;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.executor.type.JobItemExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.SimpleJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.handler.error.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.lite.handler.error.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.lite.handler.threadpool.JobExecutorServiceHandlerFactory;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.LiteJobFacade;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent.ExecutionSource;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * ElasticJob executor.
 */
@Slf4j
public final class ElasticJobExecutor {
    
    private final ElasticJob elasticJob;
    
    private final JobConfiguration jobConfig;
    
    private final JobFacade jobFacade;
    
    private final JobItemExecutor jobItemExecutor;
    
    private final ExecutorService executorService;
    
    private final JobErrorHandler jobErrorHandler;
    
    private final Map<Integer, String> itemErrorMessages;
    
    public ElasticJobExecutor(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListeners) {
        this(elasticJob, jobConfig, new LiteJobFacade(regCenter, jobConfig.getJobName(), elasticJobListeners));
    }
    
    public ElasticJobExecutor(final CoordinatorRegistryCenter regCenter, 
                              final ElasticJob elasticJob, final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListeners, final TracingConfiguration tracingConfig) {
        this(elasticJob, jobConfig, new LiteJobFacade(regCenter, jobConfig.getJobName(), elasticJobListeners, tracingConfig));
    }
    
    private ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this.elasticJob = elasticJob;
        this.jobConfig = jobConfig;
        this.jobFacade = jobFacade;
        jobItemExecutor = getJobItemExecutor(elasticJob);
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobConfig.getJobExecutorServiceHandlerType()).createExecutorService(jobConfig.getJobName());
        jobErrorHandler = JobErrorHandlerFactory.getHandler(jobConfig.getJobErrorHandlerType());
        itemErrorMessages = new ConcurrentHashMap<>(jobConfig.getShardingTotalCount(), 1);
    }
    
    @SuppressWarnings("unchecked")
    private static JobItemExecutor getJobItemExecutor(final ElasticJob elasticJob) {
        if (null == elasticJob) {
            return new ScriptJobExecutor();
        }
        if (elasticJob instanceof SimpleJob) {
            return new SimpleJobExecutor();
        }
        if (elasticJob instanceof DataflowJob) {
            return new DataflowJobExecutor();
        }
        throw new JobConfigurationException("Cannot support job type '%s'", elasticJob.getClass().getCanonicalName());
    }
    
    /**
     * Execute job.
     */
    public void execute() {
        try {
            jobFacade.checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException cause) {
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
        ShardingContexts shardingContexts = jobFacade.getShardingContexts();
        jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, String.format("Job '%s' execute begin.", jobConfig.getJobName()));
        if (jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, String.format(
                    "Previous job '%s' - shardingItems '%s' is still running, misfired job will start after previous job completed.", jobConfig.getJobName(),
                    shardingContexts.getShardingItemParameters().keySet()));
            return;
        }
        try {
            jobFacade.beforeJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
        execute(shardingContexts, ExecutionSource.NORMAL_TRIGGER);
        while (jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.clearMisfire(shardingContexts.getShardingItemParameters().keySet());
            execute(shardingContexts, ExecutionSource.MISFIRE);
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
    }
    
    private void execute(final ShardingContexts shardingContexts, final ExecutionSource executionSource) {
        if (shardingContexts.getShardingItemParameters().isEmpty()) {
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, String.format("Sharding item for job '%s' is empty.", jobConfig.getJobName()));
            return;
        }
        jobFacade.registerJobBegin(shardingContexts);
        String taskId = shardingContexts.getTaskId();
        jobFacade.postJobStatusTraceEvent(taskId, State.TASK_RUNNING, "");
        try {
            process(shardingContexts, executionSource);
        } finally {
            // TODO Consider increasing the status of job failure, and how to handle the overall loop of job failure
            jobFacade.registerJobCompleted(shardingContexts);
            if (itemErrorMessages.isEmpty()) {
                jobFacade.postJobStatusTraceEvent(taskId, State.TASK_FINISHED, "");
            } else {
                jobFacade.postJobStatusTraceEvent(taskId, State.TASK_ERROR, itemErrorMessages.toString());
            }
        }
    }
    
    private void process(final ShardingContexts shardingContexts, final ExecutionSource executionSource) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, item);
            process(shardingContexts, item, jobExecutionEvent);
            return;
        }
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (int each : items) {
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, each);
            if (executorService.isShutdown()) {
                return;
            }
            executorService.submit(() -> {
                try {
                    process(shardingContexts, each, jobExecutionEvent);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void process(final ShardingContexts shardingContexts, final int item, final JobExecutionEvent startEvent) {
        jobFacade.postJobExecutionEvent(startEvent);
        log.trace("Job '{}' executing, item is: '{}'.", jobConfig.getJobName(), item);
        JobExecutionEvent completeEvent;
        try {
            jobItemExecutor.process(elasticJob, jobConfig, jobFacade, new ShardingContext(shardingContexts, item));
            completeEvent = startEvent.executionSuccess();
            log.trace("Job '{}' executed, item is: '{}'.", jobConfig.getJobName(), item);
            jobFacade.postJobExecutionEvent(completeEvent);
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            completeEvent = startEvent.executionFailure(ExceptionUtils.transform(cause));
            jobFacade.postJobExecutionEvent(completeEvent);
            itemErrorMessages.put(item, ExceptionUtils.transform(cause));
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
    }
}
