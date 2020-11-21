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

package org.apache.shardingsphere.elasticjob.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.executor.item.JobItemExecutor;
import org.apache.shardingsphere.elasticjob.executor.item.JobItemExecutorFactory;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.infra.exception.ExceptionUtils;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.JobExecutorServiceHandlerFactory;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent.ExecutionSource;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent.State;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * ElasticJob executor.
 */
@Slf4j
public final class ElasticJobExecutor {
    
    private final ElasticJob elasticJob;
    
    private final JobFacade jobFacade;
    
    private final JobItemExecutor jobItemExecutor;
    
    private final ExecutorService executorService;
    
    private final Map<Integer, String> itemErrorMessages;
    
    public ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(elasticJob, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(elasticJob.getClass()));
    }
    
    public ElasticJobExecutor(final String type, final JobConfiguration jobConfig, final JobFacade jobFacade) {
        this(null, jobConfig, jobFacade, JobItemExecutorFactory.getExecutor(type));
    }
    
    private ElasticJobExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final JobItemExecutor jobItemExecutor) {
        this.elasticJob = elasticJob;
        this.jobFacade = jobFacade;
        this.jobItemExecutor = jobItemExecutor;
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobConfig.getJobExecutorServiceHandlerType()).createExecutorService(jobConfig.getJobName());
        itemErrorMessages = new ConcurrentHashMap<>(jobConfig.getShardingTotalCount(), 1);
    }
    
    /**
     * Execute job.
     */
    public void execute() {
        JobConfiguration jobConfig = jobFacade.loadJobConfiguration(true);
        JobErrorHandler jobErrorHandler = new JobErrorHandlerProxy(jobConfig.getJobErrorHandlerType(), jobConfig.getProps());
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
        execute(jobConfig, shardingContexts, ExecutionSource.NORMAL_TRIGGER, jobErrorHandler);
        while (jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())) {
            jobFacade.clearMisfire(shardingContexts.getShardingItemParameters().keySet());
            execute(jobConfig, shardingContexts, ExecutionSource.MISFIRE, jobErrorHandler);
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContexts);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            jobErrorHandler.handleException(jobConfig.getJobName(), cause);
        }
        try {
            jobErrorHandler.close();
        } catch (final IOException ex) {
            log.error("An error occurred when closing JobErrorHandler.", ex);
        }
    }
    
    private void execute(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final ExecutionSource executionSource, final JobErrorHandler jobErrorHandler) {
        if (shardingContexts.getShardingItemParameters().isEmpty()) {
            jobFacade.postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, String.format("Sharding item for job '%s' is empty.", jobConfig.getJobName()));
            return;
        }
        jobFacade.registerJobBegin(shardingContexts);
        String taskId = shardingContexts.getTaskId();
        jobFacade.postJobStatusTraceEvent(taskId, State.TASK_RUNNING, "");
        try {
            process(jobConfig, shardingContexts, executionSource, jobErrorHandler);
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
    
    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final ExecutionSource executionSource, final JobErrorHandler jobErrorHandler) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, item);
            process(jobConfig, shardingContexts, item, jobExecutionEvent, jobErrorHandler);
            return;
        }
        CountDownLatch latch = new CountDownLatch(items.size());
        for (int each : items) {
            JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), shardingContexts.getTaskId(), jobConfig.getJobName(), executionSource, each);
            if (executorService.isShutdown()) {
                return;
            }
            executorService.submit(() -> {
                try {
                    process(jobConfig, shardingContexts, each, jobExecutionEvent, jobErrorHandler);
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
    private void process(final JobConfiguration jobConfig, final ShardingContexts shardingContexts, final int item, final JobExecutionEvent startEvent, final JobErrorHandler jobErrorHandler) {
        jobFacade.postJobExecutionEvent(startEvent);
        log.trace("Job '{}' executing, item is: '{}'.", jobConfig.getJobName(), item);
        JobExecutionEvent completeEvent;
        try {
            jobItemExecutor.process(elasticJob, jobConfig, jobFacade, shardingContexts.createShardingContext(item));
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
    
    /**
     * Shutdown executor.
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    @RequiredArgsConstructor
    private static class JobErrorHandlerProxy implements JobErrorHandler {
        
        private final String jobErrorHandlerType;
        
        private final Properties properties;
        
        private JobErrorHandler actualJobErrorHandler;
        
        @Override
        public void handleException(final String jobName, final Throwable cause) {
            Optional.ofNullable(actualJobErrorHandler).orElseGet(this::initJobErrorHandler).handleException(jobName, cause);
        }
        
        private synchronized JobErrorHandler initJobErrorHandler() {
            if (null == actualJobErrorHandler) {
                actualJobErrorHandler = JobErrorHandlerFactory.createHandler(jobErrorHandlerType, properties)
                        .orElseThrow(() -> new JobConfigurationException("Cannot find job error handler type '%s'.", jobErrorHandlerType));
            }
            return actualJobErrorHandler;
        }
        
        @Override
        public void init(final Properties props) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public String getType() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void close() throws IOException {
            if (null != actualJobErrorHandler) {
                actualJobErrorHandler.close();
            }
        }
    }
}
