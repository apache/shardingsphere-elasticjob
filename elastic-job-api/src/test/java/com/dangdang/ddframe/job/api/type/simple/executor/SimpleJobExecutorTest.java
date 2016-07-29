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

package com.dangdang.ddframe.job.api.type.simple.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.fixture.JobCaller;
import com.dangdang.ddframe.job.api.fixture.TestFinalSimpleJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.api.internal.executor.JobExceptionHandler;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SimpleJobExecutorTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private SimpleJobExecutor simpleJobExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.loadFinalJobConfiguration()).thenReturn(new TestFinalSimpleJobConfiguration());
        simpleJobExecutor = new SimpleJobExecutor(new TestSimpleJob(jobCaller), jobFacade);
        simpleJobExecutor.setJobExceptionHandler(new JobExceptionHandler() {
            
            @Override
            public void handleException(final Throwable cause) {
                throw new JobException(cause);
            }
        });
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() {
        doThrow(TimeDiffIntolerableException.class).when(jobFacade).checkMaxTimeDiffSecondsTolerable();
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", Collections.<ShardingContext.ShardingItem>emptyList());
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems().keySet())).thenReturn(true);
        simpleJobExecutor.execute();
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", Collections.<ShardingContext.ShardingItem>emptyList());
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        simpleJobExecutor.execute();
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test(expected = JobException.class)
    public void assertExecuteWhenRunOnceAndThrowException() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        doThrow(RuntimeException.class).when(jobCaller).execute();
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
            verify(jobFacade).getShardingContext();
            verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems().keySet());
            verify(jobFacade).registerJobBegin(shardingContext);
            verify(jobCaller).execute();
            verify(jobFacade).registerJobCompleted(shardingContext);
        }
    }
    
    @Test
    public void assertExecuteWhenRunOnceSuccess() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        simpleJobExecutor.execute();
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).execute();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsEmpty() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems().keySet())).thenReturn(false);
        simpleJobExecutor.execute();
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).execute();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems().keySet())).thenReturn(false);
        when(jobFacade.isEligibleForJobRunning()).thenReturn(false);
        simpleJobExecutor.execute();
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).execute();
        verify(jobFacade, times(0)).clearMisfire(shardingContext.getShardingItems().keySet());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfire() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems().keySet())).thenReturn(true, false);
        when(jobFacade.isNeedSharding()).thenReturn(false);
        simpleJobExecutor.execute();
        verify(jobFacade).checkMaxTimeDiffSecondsTolerable();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItems().keySet());
        verify(jobFacade, times(2)).registerJobBegin(shardingContext);
        verify(jobCaller, times(2)).execute();
        verify(jobFacade, times(2)).registerJobCompleted(shardingContext);
    }
    
    @Test(expected = JobException.class)
    public void assertBeforeJobExecutedFailure() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContext);
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test(expected = JobException.class)
    public void assertAfterJobExecutedFailure() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItems().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItems().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContext);
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobCaller).execute();
        }
    }
}
