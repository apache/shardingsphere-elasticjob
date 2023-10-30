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

package org.apache.shardingsphere.elasticjob.kernel.internal.guarantee;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.base.LeaderExecutionCallback;

import java.util.Collection;

/**
 * Guarantee service.
 */
public final class GuaranteeService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService;
    
    public GuaranteeService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * Register start.
     *
     * @param shardingItems to be registered sharding items
     */
    public void registerStart(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getStartedNode(each));
        }
    }
    
    /**
     * Judge whether current sharding items are all register start success.
     *
     * @param shardingItems current sharding items
     * @return current sharding items are all start success or not
     */
    public boolean isRegisterStartSuccess(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            if (!jobNodeStorage.isJobNodeExisted(GuaranteeNode.getStartedNode(each))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge whether job's sharding items are all started.
     *
     * @return job's sharding items are all started or not
     */
    public boolean isAllStarted() {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.STARTED_ROOT)
                && configService.load(false).getShardingTotalCount() == jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.STARTED_ROOT).size();
    }
    
    /**
     * Clear all started job's info.
     */
    public void clearAllStartedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.STARTED_ROOT);
    }
    
    /**
     * Register complete.
     *
     * @param shardingItems to be registered sharding items
     */
    public void registerComplete(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            jobNodeStorage.createJobNodeIfNeeded(GuaranteeNode.getCompletedNode(each));
        }
    }
    
    /**
     * Judge whether sharding items are register complete success.
     *
     * @param shardingItems current sharding items
     * @return current sharding items are all complete success or not
     */
    public boolean isRegisterCompleteSuccess(final Collection<Integer> shardingItems) {
        for (int each : shardingItems) {
            if (!jobNodeStorage.isJobNodeExisted(GuaranteeNode.getCompletedNode(each))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge whether job's sharding items are all completed.
     *
     * @return job's sharding items are all completed or not
     */
    public boolean isAllCompleted() {
        return jobNodeStorage.isJobNodeExisted(GuaranteeNode.COMPLETED_ROOT)
                && configService.load(false).getShardingTotalCount() <= jobNodeStorage.getJobNodeChildrenKeys(GuaranteeNode.COMPLETED_ROOT).size();
    }
    
    /**
     * Clear all completed job's info.
     */
    public void clearAllCompletedInfo() {
        jobNodeStorage.removeJobNodeIfExisted(GuaranteeNode.COMPLETED_ROOT);
    }
    
    /**
     * Invoke doBeforeJobExecutedAtLastStarted method once after last started.
     *
     * @param listener         AbstractDistributeOnceElasticJobListener instance
     * @param shardingContexts sharding contexts
     */
    public void executeInLeaderForLastStarted(final AbstractDistributeOnceElasticJobListener listener,
                                              final ShardingContexts shardingContexts) {
        jobNodeStorage.executeInLeader(GuaranteeNode.STARTED_LATCH_ROOT,
                new LeaderExecutionCallbackForLastStarted(listener, shardingContexts));
    }
    
    /**
     * Invoke doAfterJobExecutedAtLastCompleted method once after last completed.
     *
     * @param listener         AbstractDistributeOnceElasticJobListener instance
     * @param shardingContexts sharding contexts
     */
    public void executeInLeaderForLastCompleted(final AbstractDistributeOnceElasticJobListener listener,
                                                final ShardingContexts shardingContexts) {
        jobNodeStorage.executeInLeader(GuaranteeNode.COMPLETED_LATCH_ROOT,
                new LeaderExecutionCallbackForLastCompleted(listener, shardingContexts));
    }
    
    /**
     * Inner class for last started callback.
     */
    @RequiredArgsConstructor
    class LeaderExecutionCallbackForLastStarted implements LeaderExecutionCallback {
        
        private final AbstractDistributeOnceElasticJobListener listener;
        
        private final ShardingContexts shardingContexts;
        
        @Override
        public void execute() {
            try {
                if (!isAllStarted()) {
                    return;
                }
                listener.doBeforeJobExecutedAtLastStarted(shardingContexts);
            } finally {
                clearAllStartedInfo();
            }
        }
    }
    
    /**
     * Inner class for last completed callback.
     */
    @RequiredArgsConstructor
    class LeaderExecutionCallbackForLastCompleted implements LeaderExecutionCallback {
        
        private final AbstractDistributeOnceElasticJobListener listener;
        
        private final ShardingContexts shardingContexts;
        
        @Override
        public void execute() {
            try {
                if (!isAllCompleted()) {
                    return;
                }
                listener.doAfterJobExecutedAtLastCompleted(shardingContexts);
            } finally {
                clearAllCompletedInfo();
            }
        }
    }
}
