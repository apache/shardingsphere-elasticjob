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

import org.apache.shardingsphere.elasticjob.cloud.config.JobRootConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.event.JobEventBus;
import org.apache.shardingsphere.elasticjob.cloud.event.type.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.cloud.event.type.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.cloud.exception.JobExecutionEnvironmentException;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Cloud job facade.
 */
@RequiredArgsConstructor
public final class CloudJobFacade implements JobFacade {
    
    private final ShardingContexts shardingContexts;
    
    private final JobConfigurationContext jobConfig;
    
    private final JobEventBus jobEventBus;
    
    @Override
    public JobRootConfiguration loadJobRootConfiguration(final boolean fromCache) {
        return jobConfig;
    }
    
    @Override
    public void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException {
    }
    
    @Override
    public void failoverIfNecessary() {
    }
    
    @Override
    public void registerJobBegin(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
    }

    /**
     * Get sharding contexts.
     * @return sharding contexts
     */
    public ShardingContexts getShardingContexts() {
        return shardingContexts;
    }
    
    @Override
    public boolean misfireIfRunning(final Collection<Integer> shardingItems) {
        return false;
    }
    
    @Override
    public void clearMisfire(final Collection<Integer> shardingItems) {
    }
    
    @Override
    public boolean isExecuteMisfired(final Collection<Integer> shardingItems) {
        return false;
    }
    
    @Override
    public boolean isEligibleForJobRunning() {
        return jobConfig.getTypeConfig() instanceof DataflowJobConfiguration && ((DataflowJobConfiguration) jobConfig.getTypeConfig()).isStreamingProcess();
    }
    
    @Override
    public boolean isNeedSharding() {
        return false;
    }
    
    @Override
    public void beforeJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void afterJobExecuted(final ShardingContexts shardingContexts) {
    }
    
    @Override
    public void postJobExecutionEvent(final JobExecutionEvent jobExecutionEvent) {
        jobEventBus.post(jobExecutionEvent);
    }
    
    @Override
    public void postJobStatusTraceEvent(final String taskId, final JobStatusTraceEvent.State state, final String message) {
        TaskContext taskContext = TaskContext.from(taskId);
        jobEventBus.post(new JobStatusTraceEvent(taskContext.getMetaInfo().getJobName(), taskContext.getId(), taskContext.getSlaveId(), 
                JobStatusTraceEvent.Source.CLOUD_EXECUTOR, taskContext.getType(), String.valueOf(taskContext.getMetaInfo().getShardingItems()), state, message));
    }
}
