/**
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
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;

/**
 * 弹性化分布式作业的基类.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public abstract class AbstractElasticJob implements ElasticJob {
    
    @Getter(AccessLevel.PROTECTED)
    private volatile boolean stoped;
    
    @Getter(AccessLevel.PROTECTED)
    private ConfigurationService configService;
    
    @Setter
    @Getter(AccessLevel.PROTECTED)
    private ShardingService shardingService;
    
    @Setter
    private ExecutionContextService executionContextService;
    
    @Setter
    private ExecutionService executionService;
    
    @Setter
    private FailoverService failoverService;
    
    @Setter
    @Getter(AccessLevel.PROTECTED)
    private OffsetService offsetService;
    
    @Setter
    private List<ElasticJobListener> elasticJobListeners = new ArrayList<>();
    
    @Override
    public final void execute(final JobExecutionContext context) throws JobExecutionException {
        log.trace("Elastic job: job execute begin, job execution context:{}.", context);
        configService.checkMaxTimeDiffSecondsTolerable();
        shardingService.shardingIfNecessary();
        JobExecutionMultipleShardingContext shardingContext = executionContextService.getJobExecutionShardingContext();
        if (executionService.misfireIfNecessary(shardingContext.getShardingItems())) {
            log.debug("Elastic job: previous job is still running, new job will start after previous job completed. Misfired job had recorded.");
            return;
        }
        if (!elasticJobListeners.isEmpty()) {
            for (ElasticJobListener each : elasticJobListeners) {
                try {
                    each.beforeJobExecuted(shardingContext); 
                } catch (final JobException ex) {
                    handleJobExecutionException(new JobExecutionException(ex));
                }
            }
        }
        executeJobInternal(shardingContext);
        log.trace("Elastic job: execute normal completed, sharding context:{}.", shardingContext);
        while (configService.isMisfire() && !executionService.getMisfiredJobItems(shardingContext.getShardingItems()).isEmpty() && !stoped && !shardingService.isNeedSharding()) {
            log.trace("Elastic job: execute misfired job, sharding context:{}.", shardingContext);
            executionService.clearMisfire(shardingContext.getShardingItems());
            executeJobInternal(shardingContext);
            log.trace("Elastic job: misfired job completed, sharding context:{}.", shardingContext);
        }
        if (configService.isFailover() && !stoped) {
            failoverService.failoverIfNecessary();
        }
        if (!elasticJobListeners.isEmpty()) {
            for (ElasticJobListener each : elasticJobListeners) {
                try {
                    each.afterJobExecuted(shardingContext);
                } catch (final JobException ex) {
                    handleJobExecutionException(new JobExecutionException(ex));
                }
            }
        }
        log.trace("Elastic job: execute all completed, job execution context:{}.", context);
    }
    
    private void executeJobInternal(final JobExecutionMultipleShardingContext shardingContext) throws JobExecutionException {
        if (shardingContext.getShardingItems().isEmpty()) {
            log.trace("Elastic job: sharding item is empty, job execution context:{}.", shardingContext);
            return;
        }
        executionService.registerJobBegin(shardingContext);
        try {
            executeJob(shardingContext);
        //CHECKSTYLE:OFF
        } catch (final Exception ex) {
        //CHECKSTYLE:ON
            handleJobExecutionException(new JobExecutionException(ex));
        } finally {
            // TODO 考虑增加作业失败的状态，并且考虑如何处理作业失败的整体回路
            executionService.registerJobCompleted(shardingContext);
            if (configService.isFailover()) {
                failoverService.updateFailoverComplete(shardingContext.getShardingItems());
            }
        }
    }
    
    protected abstract void executeJob(final JobExecutionMultipleShardingContext shardingContext);
    
    @Override
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
        throw jobExecutionException;
    }
    
    @Override
    public final void stop() {
        stoped = true;
    }
    
    @Override
    public final void resume() {
        stoped = false;
    }
    
    public final void setConfigService(final ConfigurationService configService) {
        this.configService = configService;
        JobRegistry.getInstance().addJobInstance(configService.getJobName(), this);
    }
}
