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

package org.apache.shardingsphere.elasticjob.lite.api.listener;

import lombok.Setter;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.infra.env.TimeService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;

import java.util.Set;

/**
 * Distributed once elasticjob listener.
 */
public abstract class AbstractDistributeOnceElasticJobListener implements ElasticJobListener {
    
    private final long startedTimeoutMilliseconds;
    
    private final Object startedWait = new Object();
    
    private final long completedTimeoutMilliseconds;
    
    private final Object completedWait = new Object();
    
    @Setter
    private GuaranteeService guaranteeService;
    
    private final TimeService timeService = new TimeService();
    
    public AbstractDistributeOnceElasticJobListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        this.startedTimeoutMilliseconds = startedTimeoutMilliseconds <= 0L ? Long.MAX_VALUE : startedTimeoutMilliseconds;
        this.completedTimeoutMilliseconds = completedTimeoutMilliseconds <= 0L ? Long.MAX_VALUE : completedTimeoutMilliseconds;
    }
    
    @Override
    public final void beforeJobExecuted(final ShardingContexts shardingContexts) {
        Set<Integer> shardingItems = shardingContexts.getShardingItemParameters().keySet();
        if (shardingItems.isEmpty()) {
            return;
        }
        guaranteeService.registerStart(shardingItems);
        while (!guaranteeService.isRegisterStartSuccess(shardingItems)) {
            BlockUtils.waitingShortTime();
        }
        if (guaranteeService.isAllStarted()) {
            doBeforeJobExecutedAtLastStarted(shardingContexts);
            guaranteeService.clearAllStartedInfo();
            return;
        }
        long before = timeService.getCurrentMillis();
        try {
            synchronized (startedWait) {
                startedWait.wait(startedTimeoutMilliseconds);
            }
        } catch (final InterruptedException ex) {
            Thread.interrupted();
        }
        if (timeService.getCurrentMillis() - before >= startedTimeoutMilliseconds) {
            guaranteeService.clearAllStartedInfo();
            handleTimeout(startedTimeoutMilliseconds);
        }
    }
    
    @Override
    public final void afterJobExecuted(final ShardingContexts shardingContexts) {
        Set<Integer> shardingItems = shardingContexts.getShardingItemParameters().keySet();
        if (shardingItems.isEmpty()) {
            return;
        }
        guaranteeService.registerComplete(shardingItems);
        while (!guaranteeService.isRegisterCompleteSuccess(shardingItems)) {
            BlockUtils.waitingShortTime();
        }
        if (guaranteeService.isAllCompleted()) {
            doAfterJobExecutedAtLastCompleted(shardingContexts);
            guaranteeService.clearAllCompletedInfo();
            return;
        }
        long before = timeService.getCurrentMillis();
        try {
            synchronized (completedWait) {
                completedWait.wait(completedTimeoutMilliseconds);
            }
        } catch (final InterruptedException ex) {
            Thread.interrupted();
        }
        if (timeService.getCurrentMillis() - before >= completedTimeoutMilliseconds) {
            guaranteeService.clearAllCompletedInfo();
            handleTimeout(completedTimeoutMilliseconds);
        }
    }
    
    private void handleTimeout(final long timeoutMilliseconds) {
        throw new JobSystemException("Job timeout. timeout mills is %s.", timeoutMilliseconds);
    }
    
    /**
     * Do before job executed at last sharding job started.
     *
     * @param shardingContexts sharding contexts
     */
    public abstract void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts);
    
    /**
     * Do after job executed at last sharding job completed.
     *
     * @param shardingContexts sharding contexts
     */
    public abstract void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts);
    
    /**
     * Notify waiting task start.
     */
    public void notifyWaitingTaskStart() {
        synchronized (startedWait) {
            startedWait.notifyAll();
        }
    }
    
    /**
     * Notify waiting task complete.
     */
    public void notifyWaitingTaskComplete() {
        synchronized (completedWait) {
            completedWait.notifyAll();
        }
    }
}
