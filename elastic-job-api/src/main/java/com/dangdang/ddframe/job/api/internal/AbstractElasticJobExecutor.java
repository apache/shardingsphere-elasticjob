/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.api.internal;

import com.dangdang.ddframe.job.api.JobExceptionHandler;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.exception.JobException;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 弹性化分布式作业执行器.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractElasticJobExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final JobFacade jobFacade;
    
    private Optional<JobExceptionHandler> jobExceptionHandler = Optional.absent();
    
    /**
     * 执行作业.
     */
    public final void execute() {
        log.trace("Elastic job: job execute begin.");
        jobFacade.checkMaxTimeDiffSecondsTolerable();
        ShardingContext shardingContext = jobFacade.getShardingContext();
        if (jobFacade.misfireIfNecessary(shardingContext.getShardingItems().keySet())) {
            log.debug("Elastic job: previous job is still running, new job will start after previous job completed. Misfired job had recorded.");
            return;
        }
        jobFacade.cleanPreviousExecutionInfo();
        try {
            jobFacade.beforeJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            handleException(new JobException(cause));
        }
        execute(shardingContext);
        log.trace("Elastic job: execute normal completed, sharding context:{}.", shardingContext);
        while (jobFacade.isExecuteMisfired(shardingContext.getShardingItems().keySet())) {
            log.trace("Elastic job: execute misfired job, sharding context:{}.", shardingContext);
            jobFacade.clearMisfire(shardingContext.getShardingItems().keySet());
            execute(shardingContext);
            log.trace("Elastic job: misfired job completed, sharding context:{}.", shardingContext);
        }
        jobFacade.failoverIfNecessary();
        try {
            jobFacade.afterJobExecuted(shardingContext);
            //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            //CHECKSTYLE:ON
            handleException(new JobException(cause));
        }
        log.trace("Elastic job: execute all completed.");
    }
    
    private void execute(final ShardingContext shardingContext) {
        if (shardingContext.getShardingItems().isEmpty()) {
            log.trace("Elastic job: sharding item is empty, job execution context:{}.", shardingContext);
            return;
        }
        jobFacade.registerJobBegin(shardingContext);
        try {
            process(shardingContext);
        //CHECKSTYLE:OFF
        } catch (final Throwable cause) {
        //CHECKSTYLE:ON
            handleException(cause);
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            jobFacade.registerJobCompleted(shardingContext);
        }
    }
    
    protected abstract void process(final ShardingContext shardingContext);
    
    protected void handleException(final Throwable cause) {
        if (jobExceptionHandler.isPresent()) {
            jobExceptionHandler.get().handleException(cause);
        } else {
            log.error("Elastic job: exception occur in job processing...", cause);
        }
    }
    
    /**
     * 设置作业异常处理器.
     *
     * @param jobExceptionHandler 作业异常处理器
     */
    public void setJobExceptionHandler(final JobExceptionHandler jobExceptionHandler) {
        this.jobExceptionHandler = Optional.of(jobExceptionHandler);
    }
}
