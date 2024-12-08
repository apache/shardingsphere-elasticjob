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

package org.apache.shardingsphere.elasticjob.kernel.executor.facade;

import com.google.common.base.Strings;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.context.TaskContext;
import org.apache.shardingsphere.elasticjob.kernel.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.spi.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobStatusTraceEvent.State;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract Job facade.
 */
@Slf4j
abstract class AbstractJobFacade implements JobFacade {

    protected final ConfigurationService configService;

    protected final ShardingService shardingService;

    protected final ExecutionContextService executionContextService;

    protected final ExecutionService executionService;

    protected final FailoverService failoverService;

    protected final Collection<ElasticJobListener> elasticJobListeners;

    protected final JobTracingEventBus jobTracingEventBus;

    public AbstractJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners, final TracingConfiguration<?> tracingConfig) {
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionContextService = new ExecutionContextService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        this.elasticJobListeners = elasticJobListeners.stream().sorted(Comparator.comparingInt(ElasticJobListener::order)).collect(Collectors.toList());
        this.jobTracingEventBus = null == tracingConfig ? new JobTracingEventBus() : new JobTracingEventBus(tracingConfig);
    }
    
    /**
     * Load job configuration.
     *
     * @param fromCache load from cache or not
     * @return job configuration
     */
    @Override
    public JobConfiguration loadJobConfiguration(final boolean fromCache) {
        return configService.load(fromCache);
    }
    
    /**
     * Check job execution environment.
     *
     * @throws org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException job execution environment exception
     */
    @Override
    public void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException {
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    /**
     * Failover If necessary.
     */
    @Override
    public void failoverIfNecessary() {
        if (configService.load(true).isFailover()) {
            failoverService.failoverIfNecessary();
        }
    }
    
    /**
     * Register job begin.
     *
     * @param shardingContexts sharding contexts
     */
    @Override
    public void registerJobBegin(final ShardingContexts shardingContexts) {
        executionService.registerJobBegin(shardingContexts);
    }
    
    /**
     * Register job completed.
     *
     * @param shardingContexts sharding contexts
     */
    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        executionService.registerJobCompleted(shardingContexts);
        if (configService.load(true).isFailover()) {
            failoverService.updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        }
    }
    
    public abstract ShardingContexts getShardingContexts();
    
    /**
     * Set task misfire flag.
     *
     * @param shardingItems sharding items to be set misfire flag
     * @return whether satisfy misfire condition
     */
    @Override
    public boolean misfireIfRunning(final Collection<Integer> shardingItems) {
        return executionService.misfireIfHasRunningItems(shardingItems);
    }
    
    /**
     * Clear misfire flag.
     *
     * @param shardingItems sharding items to be cleared misfire flag
     */
    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
        executionService.clearMisfire(shardingItems);
    }
    
    /**
     * Judge job whether to need to execute misfire tasks.
     *
     * @param shardingItems sharding items
     * @return need to execute misfire tasks or not
     */
    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return configService.load(true).isMisfire() && !isNeedSharding() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    /**
     * Judge job whether to need resharding.
     *
     * @return need resharding or not
     */
    @Override
    public boolean isNeedSharding() {
        return shardingService.isNeedSharding();
    }
    
    /**
     * Call before job executed.
     *
     * @param shardingContexts sharding contexts
     */
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.beforeJobExecuted(shardingContexts);
        }
    }
    
    /**
     * Call after job executed.
     *
     * @param shardingContexts sharding contexts
     */
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.afterJobExecuted(shardingContexts);
        }
    }
    
    /**
     * Post job execution event.
     *
     * @param jobExecutionEvent job execution event
     */
    @Override
    public void postJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        jobTracingEventBus.post(jobExecutionEvent);
    }
    
    /**
     * Post job status trace event.
     *
     * @param taskId task Id
     * @param state job state
     * @param message job message
     */
    @Override
    public void postJobStatusTraceEvent(final String taskId, final State state, final String message) {
        TaskContext taskContext = TaskContext.from(taskId);
        jobTracingEventBus.post(new JobStatusTraceEvent(taskContext.getMetaInfo().getJobName(), taskContext.getId(),
                taskContext.getSlaveId(), taskContext.getType(), taskContext.getMetaInfo().getShardingItems().toString(), state, message));
        if (!Strings.isNullOrEmpty(message)) {
            log.trace(message);
        }
    }
    
    /**
     * Get job runtime service.
     *
     * @return job runtime service
     */
    @Override
    public JobRuntimeService getJobRuntimeService() {
        return new JobJobRuntimeServiceImpl(this);
    }
}
