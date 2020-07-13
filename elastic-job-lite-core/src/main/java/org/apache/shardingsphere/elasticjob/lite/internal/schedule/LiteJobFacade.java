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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.context.TaskContext;
import org.apache.shardingsphere.elasticjob.lite.dag.DagService;
import org.apache.shardingsphere.elasticjob.lite.dag.DagStates;
import org.apache.shardingsphere.elasticjob.lite.dag.JobDagConfig;
import org.apache.shardingsphere.elasticjob.lite.exception.DagRuntimeException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent.Source;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lite job facade.
 */
@Slf4j
public final class LiteJobFacade implements JobFacade {
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    private final JobEventBus jobEventBus;

    private final DagService dagService;
    
    public LiteJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners, final JobEventBus jobEventBus) {
        this(regCenter, jobName, elasticJobListeners, jobEventBus, null);
    }

    public LiteJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners, final JobEventBus jobEventBus,
                         final JobDagConfig jobDagConfig) {
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionContextService = new ExecutionContextService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        this.elasticJobListeners = elasticJobListeners;
        this.jobEventBus = jobEventBus;
        if (null == jobDagConfig) {
            this.dagService = null;
        } else {
            this.dagService = new DagService(regCenter, jobName, jobEventBus, jobDagConfig);
        }
    }

    @Override
    public JobConfiguration loadJobConfiguration(final boolean fromCache) {
        return configService.load(fromCache);
    }
    
    @Override
    public void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException {
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Override
    public void failoverIfNecessary() {
        if (configService.load(true).isFailover()) {
            failoverService.failoverIfNecessary();
        }
    }
    
    @Override
    public void registerJobBegin(final ShardingContexts shardingContexts) {
        executionService.registerJobBegin(shardingContexts);
    }
    
    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        executionService.registerJobCompleted(shardingContexts);
        if (configService.load(true).isFailover()) {
            failoverService.updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        }
    }

    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts, final Map<Integer, String> itemErrorMessages) {
        executionService.registerJobCompleted(shardingContexts, itemErrorMessages);
        if (configService.load(true).isFailover()) {
            failoverService.updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        }
    }

    @Override
    public ShardingContexts getShardingContexts() {
        boolean isFailover = configService.load(true).isFailover();
        if (isFailover) {
            List<Integer> failoverShardingItems = failoverService.getLocalFailoverItems();
            if (!failoverShardingItems.isEmpty()) {
                return executionContextService.getJobShardingContext(failoverShardingItems);
            }
        }
        shardingService.shardingIfNecessary();
        List<Integer> shardingItems = shardingService.getLocalShardingItems();
        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalTakeOffItems());
        }
        shardingItems.removeAll(executionService.getDisabledItems(shardingItems));
        return executionContextService.getJobShardingContext(shardingItems);
    }
    
    @Override
    public boolean misfireIfRunning(final Collection<Integer> shardingItems) {
        return executionService.misfireIfHasRunningItems(shardingItems);
    }
    
    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
        executionService.clearMisfire(shardingItems);
    }
    
    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return isEligibleForJobRunning() && configService.load(true).isMisfire() && !executionService.getMisfiredJobItems(shardingItems).isEmpty();
    }
    
    @Override
    public boolean isEligibleForJobRunning() {
        JobConfiguration jobConfig = configService.load(true);
        if (JobType.DATAFLOW == jobConfig.getJobType()) {
            return !shardingService.isNeedSharding()
                    && Boolean.parseBoolean(jobConfig.getProps().getOrDefault(DataflowJobExecutor.STREAM_PROCESS_KEY, false).toString());
        }
        return !shardingService.isNeedSharding();
    }
    
    @Override
    public boolean isNeedSharding() {
        return shardingService.isNeedSharding();
    }
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.beforeJobExecuted(shardingContexts);
        }
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
        for (ElasticJobListener each : elasticJobListeners) {
            each.afterJobExecuted(shardingContexts);
        }
    }
    
    @Override
    public void postJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        jobEventBus.post(jobExecutionEvent);
    }
    
    @Override
    public void postJobStatusTraceEvent(final String taskId, final State state, final String message) {
        TaskContext taskContext = TaskContext.from(taskId);
        jobEventBus.post(new JobStatusTraceEvent(taskContext.getMetaInfo().getJobName(), taskContext.getId(),
                taskContext.getSlaveId(), Source.LITE_EXECUTOR, taskContext.getType().name(), taskContext.getMetaInfo().getShardingItems().toString(), state, message));
        if (!Strings.isNullOrEmpty(message)) {
            log.trace(message);
        }
    }

    @Override
    public boolean isDagJob() {
        return dagService != null;
    }

    @Override
    public void dagStatesCheck() {
        if (dagService.getDagStates() == DagStates.RUNNING) {
            return;
        }
        if (dagService.getDagStates() == DagStates.PAUSE) {
            throw new DagRuntimeException("Dag Job states PAUSE");
        }

        if (dagService.isDagRootJob()) {
            dagService.changeDagStatesAndReGraph();
        }

        if (dagService.getDagStates() != DagStates.RUNNING) {
            log.info("DAG states not right {}", dagService.getDagStates().getValue());
            throw new DagRuntimeException("Dag states Not right!");
        }
    }

    @Override
    public void dagJobDependenciesCheck() {
        dagService.checkJobDependenciesState();
    }

    @Override
    public void initDagConfig() {
        if (null != dagService) {
            dagService.regDagConfig();
        }
    }
}
