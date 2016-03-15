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

package com.dangdang.ddframe.job.internal.job;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 弹性化分布式作业的基类.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public abstract class AbstractElasticJob implements ElasticJob {
    
    @Getter(AccessLevel.PROTECTED)
    private volatile boolean stopped;
    
    @Getter(AccessLevel.PROTECTED)
    private SchedulerFacade schedulerFacade;
    
    @Override
    public final void execute(final JobExecutionContext context) throws JobExecutionException {
        log.trace("Elastic job: job execute begin, job execution context:{}.", context);
        schedulerFacade.checkMaxTimeDiffSecondsTolerable();
        JobExecutionMultipleShardingContext shardingContext = schedulerFacade.getShardingContext();
        if (schedulerFacade.misfireIfNecessary(shardingContext.getShardingItems())) {
            log.debug("Elastic job: previous job is still running, new job will start after previous job completed. Misfired job had recorded.");
            return;
        }
        try {
            schedulerFacade.beforeJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            handleJobExecutionException(new JobExecutionException(cause));
        }
        executeJobInternal(shardingContext);
        log.trace("Elastic job: execute normal completed, sharding context:{}.", shardingContext);
        while (schedulerFacade.isExecuteMisfired(stopped, shardingContext.getShardingItems())) {
            log.trace("Elastic job: execute misfired job, sharding context:{}.", shardingContext);
            schedulerFacade.clearMisfire(shardingContext.getShardingItems());
            executeJobInternal(shardingContext);
            log.trace("Elastic job: misfired job completed, sharding context:{}.", shardingContext);
        }
        schedulerFacade.failoverIfNecessary(stopped);
        try {
            schedulerFacade.afterJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            handleJobExecutionException(new JobExecutionException(cause));
        }
        log.trace("Elastic job: execute all completed, job execution context:{}.", context);
    }
    
    private void executeJobInternal(final JobExecutionMultipleShardingContext shardingContext) throws JobExecutionException {
        if (shardingContext.getShardingItems().isEmpty()) {
            log.trace("Elastic job: sharding item is empty, job execution context:{}.", shardingContext);
            return;
        }
        schedulerFacade.registerJobBegin(shardingContext);
        try {
            executeJob(shardingContext);
        //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
        //CHECKSTYLE:ON
            handleJobExecutionException(new JobExecutionException(cause));
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            schedulerFacade.registerJobCompleted(shardingContext);
        }
    }
    
    protected abstract void executeJob(final JobExecutionMultipleShardingContext shardingContext);
    
    @Override
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
        throw jobExecutionException;
    }
    
    @Override
    public final void stop() {
        stopped = true;
    }
    
    @Override
    public final void resume() {
        stopped = false;
    }
    
    public final void setSchedulerFacade(final SchedulerFacade schedulerFacade) {
        this.schedulerFacade = schedulerFacade;
        JobRegistry.getInstance().addJobInstance(schedulerFacade.getJobName(), this);
    }
}
