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

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticJobAssert {
    
    public static final String JOB_NAME = "unit_test_job";
    
    public static JobExecutionMultipleShardingContext getShardingContext() {
        JobExecutionMultipleShardingContext result = new JobExecutionMultipleShardingContext();
        result.setJobName(JOB_NAME);
        result.setShardingItems(Arrays.asList(0, 1));
        return result;
    }
    
    public static void prepareForIsNotMisfire(final JobFacade jobFacade, final JobExecutionMultipleShardingContext shardingContext) {
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems())).thenReturn(false);
    }
    
    public static void verifyForIsNotMisfire(final JobFacade jobFacade, final JobExecutionMultipleShardingContext shardingContext) {
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems());
        verify(jobFacade).beforeJobExecuted(shardingContext);
        verify(jobFacade).registerJobBegin(shardingContext);
        verify(jobFacade).registerJobCompleted(shardingContext);
        verify(jobFacade).isExecuteMisfired(shardingContext.getShardingItems());
        verify(jobFacade).afterJobExecuted(shardingContext);
    }
    
    public static void assertProcessCountStatistics(final int successCount, final int failureCount) {
        assertThat(ProcessCountStatistics.getProcessSuccessCount(JOB_NAME), is(successCount));
        assertThat(ProcessCountStatistics.getProcessFailureCount(JOB_NAME), is(failureCount));
    }
}
