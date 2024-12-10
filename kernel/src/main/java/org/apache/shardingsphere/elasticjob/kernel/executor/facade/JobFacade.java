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

import java.util.Collection;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobStatusTraceEvent;

/**
 * Job facade.
 */
public interface JobFacade {
    
    /**
     * Load job configuration.
     * 
     * @param fromCache load from cache or not
     * @return job configuration
     */
    JobConfiguration loadJobConfiguration(boolean fromCache);
    
    /**
     * Check job execution environment.
     * 
     * @throws JobExecutionEnvironmentException job execution environment exception
     */
    void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException;
    
    /**
     * Failover If necessary.
     */
    void failoverIfNecessary();
    
    /**
     * Register job begin.
     *
     * @param shardingContexts sharding contexts
     */
    void registerJobBegin(ShardingContexts shardingContexts);
    
    /**
     * Register job completed.
     *
     * @param shardingContexts sharding contexts
     */
    void registerJobCompleted(ShardingContexts shardingContexts);
    
    /**
     * Get sharding contexts.
     *
     * @return sharding contexts
     */
    ShardingContexts getShardingContexts();
    
    /**
     * Set task misfire flag.
     *
     * @param shardingItems sharding items to be set misfire flag
     * @return whether satisfy misfire condition
     */
    boolean misfireIfRunning(Collection<Integer> shardingItems);
    
    /**
     * Clear misfire flag.
     *
     * @param shardingItems sharding items to be cleared misfire flag
     */
    void clearMisfire(Collection<Integer> shardingItems);
    
    /**
     * Judge job whether need to execute misfire tasks.
     * 
     * @param shardingItems sharding items
     * @return whether need to execute misfire tasks
     */
    boolean isExecuteMisfired(Collection<Integer> shardingItems);
    
    /**
     * Judge job whether need resharding.
     *
     * @return whether need resharding
     */
    boolean isNeedSharding();
    
    /**
     * Call before job executed.
     *
     * @param shardingContexts sharding contexts
     */
    void beforeJobExecuted(ShardingContexts shardingContexts);
    
    /**
     * Call after job executed.
     *
     * @param shardingContexts sharding contexts
     */
    void afterJobExecuted(ShardingContexts shardingContexts);
    
    /**
     * Post job execution event.
     *
     * @param jobExecutionEvent job execution event
     */
    void postJobExecutionEvent(JobExecutionEvent jobExecutionEvent);
    
    /**
     * Post job status trace event.
     *
     * @param taskId task Id
     * @param state job state
     * @param message job message
     */
    void postJobStatusTraceEvent(String taskId, JobStatusTraceEvent.State state, String message);
    
    /**
     * Get job runtime service.
     *
     * @return job runtime service
     */
    JobRuntimeService getJobRuntimeService();
}
