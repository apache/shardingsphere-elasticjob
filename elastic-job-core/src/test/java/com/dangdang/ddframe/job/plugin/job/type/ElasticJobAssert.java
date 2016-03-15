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

package com.dangdang.ddframe.job.plugin.job.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticJobAssert {
    
    public static final String JOB_NAME = "unit_test_job";
    
    public static JobExecutionMultipleShardingContext getShardingContext() {
        JobExecutionMultipleShardingContext result = new JobExecutionMultipleShardingContext();
        result.setJobName(JOB_NAME);
        result.setShardingItems(Arrays.asList(0, 1));
        return result;
    }
    
    public static void prepareForIsNotMisfire(final SchedulerFacade schedulerFacade, final JobExecutionMultipleShardingContext shardingContext) {
        when(schedulerFacade.getShardingContext()).thenReturn(shardingContext);
        when(schedulerFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(schedulerFacade.isExecuteMisfired(false, shardingContext.getShardingItems())).thenReturn(false);
    }
    
    public static void verifyForIsNotMisfireAndNotStopped(final SchedulerFacade schedulerFacade, final JobExecutionMultipleShardingContext shardingContext) {
        verifyForIsNotMisfire(schedulerFacade, shardingContext, false);
    }
    
    public static void verifyForIsNotMisfireAndStopped(final SchedulerFacade schedulerFacade, final JobExecutionMultipleShardingContext shardingContext) {
        verifyForIsNotMisfire(schedulerFacade, shardingContext, true);
    }
    
    private static void verifyForIsNotMisfire(final SchedulerFacade schedulerFacade, final JobExecutionMultipleShardingContext shardingContext, final boolean stopped) {
        verify(schedulerFacade).checkMaxTimeDiffSecondsTolerable();
        verify(schedulerFacade).getShardingContext();
        verify(schedulerFacade).misfireIfNecessary(shardingContext.getShardingItems());
        verify(schedulerFacade).beforeJobExecuted(shardingContext);
        verify(schedulerFacade).registerJobBegin(shardingContext);
        verify(schedulerFacade).registerJobCompleted(shardingContext);
        verify(schedulerFacade).isExecuteMisfired(stopped, shardingContext.getShardingItems());
        verify(schedulerFacade).failoverIfNecessary(stopped);
        verify(schedulerFacade).afterJobExecuted(shardingContext);
    }
    
    public static void assertProcessCountStatistics(final int successCount, final int failureCount) {
        assertThat(ProcessCountStatistics.getProcessSuccessCount(JOB_NAME), is(successCount));
        assertThat(ProcessCountStatistics.getProcessFailureCount(JOB_NAME), is(failureCount));
    }
}
