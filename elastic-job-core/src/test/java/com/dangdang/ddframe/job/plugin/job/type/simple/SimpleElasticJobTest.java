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

package com.dangdang.ddframe.job.plugin.job.type.simple;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.fixture.FooSimpleElasticJob;
import com.dangdang.ddframe.job.plugin.job.type.fixture.JobCaller;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SimpleElasticJobTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private FooSimpleElasticJob simpleElasticJob;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.getJobName()).thenReturn("testJob");
        simpleElasticJob = new FooSimpleElasticJob(jobCaller);
        simpleElasticJob.setJobFacade(jobFacade);
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() throws JobExecutionException {
        doThrow(TimeDiffIntolerableException.class).when(jobFacade).checkMaxTimeDiffSecondsTolerable();
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
            verify(jobCaller, times(0)).process();
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(true);
        simpleElasticJob.execute(null);
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems());
        verify(jobCaller, times(0)).process();
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        simpleElasticJob.execute(null);
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems());
        verify(jobCaller, times(0)).process();
    }
    
    @Test(expected = JobExecutionException.class)
    public void assertExecuteWhenRunOnceAndThrowException() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        doThrow(RuntimeException.class).when(jobCaller).process();
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
            verify(jobFacade).getShardingContext();
            verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems());
            verify(jobFacade).registerJobBegin(shardingContext);
            verify(jobCaller).process();
            verify(jobFacade).registerJobCompleted(shardingContext);
        }
    }
    
    @Test
    public void assertExecuteWhenRunOnceSuccess() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).process();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsEmpty() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems())).thenReturn(false);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).process();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems())).thenReturn(false);
        when(jobFacade.isEligibleForJobRunning()).thenReturn(false);
        simpleElasticJob.execute(null);
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).process();
        verify(jobFacade, times(0)).clearMisfire(shardingContext.getShardingItems());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfire() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems())).thenReturn(true, false);
        when(jobFacade.isNeedSharding()).thenReturn(false);
        simpleElasticJob.execute(null);
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems());
        verify(jobFacade, times(2)).registerJobBegin(shardingContext);
        verify(jobCaller, times(2)).process();
        verify(jobFacade, times(2)).registerJobCompleted(shardingContext);
    }
    
    @Test(expected = JobExecutionException.class)
    public void assertBeforeJobExecutedFailure() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContext);
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(jobCaller, times(0)).process();
        }
    }
    
    @Test(expected = JobExecutionException.class)
    public void assertAfterJobExecutedFailure() throws JobExecutionException {
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContext);
        try {
            simpleElasticJob.execute(null);
        } finally {
            verify(jobCaller).process();
        }
    }
}
