/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.api.listener;

import com.dangdang.ddframe.job.cloud.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.cloud.exception.JobTimeoutException;
import com.dangdang.ddframe.job.cloud.internal.env.TimeService;
import com.dangdang.ddframe.job.cloud.internal.guarantee.GuaranteeService;
import lombok.Setter;

/**
 * 在分布式作业中只执行一次的监听器.
 * 
 * @author zhangliang
 */
public abstract class AbstractDistributeOnceElasticJobListener implements ElasticJobListener {
    
    private final long startedTimeoutMilliseconds;
    
    private final Object startedWait = new Object();
    
    private final long completedTimeoutMilliseconds;
    
    private final Object completedWait = new Object();
    
    @Setter
    private GuaranteeService guaranteeService;
    
    private TimeService timeService = new TimeService();
    
    public AbstractDistributeOnceElasticJobListener(final long startedTimeoutMilliseconds, final long completedTimeoutMilliseconds) {
        if (startedTimeoutMilliseconds <= 0L) {
            this.startedTimeoutMilliseconds = Long.MAX_VALUE;
        } else {
            this.startedTimeoutMilliseconds = startedTimeoutMilliseconds;
        }
        if (completedTimeoutMilliseconds <= 0L) {
            this.completedTimeoutMilliseconds = Long.MAX_VALUE; 
        } else {
            this.completedTimeoutMilliseconds = completedTimeoutMilliseconds;
        }
    }
    
    @Override
    public final void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        guaranteeService.registerStart(shardingContext.getShardingItems());
        if (guaranteeService.isAllStarted()) {
            doBeforeJobExecutedAtLastStarted(shardingContext);
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
            throw new JobTimeoutException(startedTimeoutMilliseconds);
        }
    }
    
    @Override
    public final void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        guaranteeService.registerComplete(shardingContext.getShardingItems());
        if (guaranteeService.isAllCompleted()) {
            doAfterJobExecutedAtLastCompleted(shardingContext);
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
            throw new JobTimeoutException(completedTimeoutMilliseconds);
        }
    }
    
    /**
     * 分布式环境中最后一个作业执行前的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public abstract void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext);
    
    /**
     * 分布式环境中最后一个作业执行后的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    public abstract void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext);
    
    /**
     * 通知任务开始.
     */
    public void notifyWaitingTaskStart() {
        synchronized (startedWait) {
            startedWait.notifyAll();
        }
    }
    
    /**
     * 通知任务结束.
     */
    public void notifyWaitingTaskComplete() {
        synchronized (completedWait) {
            completedWait.notifyAll();
        }
    }
}
