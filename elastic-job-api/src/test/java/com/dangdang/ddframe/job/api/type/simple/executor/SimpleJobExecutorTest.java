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
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.exception.JobSystemException;
import com.dangdang.ddframe.job.api.fixture.config.TestSimpleJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.job.JobCaller;
import com.dangdang.ddframe.job.api.fixture.job.TestSimpleJob;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
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
        when(jobFacade.loadJobConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        simpleJobExecutor = new SimpleJobExecutor(new TestSimpleJob(jobCaller), jobFacade);
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() throws JobExecutionEnvironmentException {
        doThrow(JobExecutionEnvironmentException.class).when(jobFacade).checkJobExecutionEnvironment();
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobFacade).checkJobExecutionEnvironment();
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() throws JobExecutionEnvironmentException {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", Collections.<Integer, String>emptyMap());
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())).thenReturn(true);
        simpleJobExecutor.execute();
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItemParameters().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() throws JobExecutionEnvironmentException {
        ShardingContext shardingContext = new ShardingContext("test_job", 10, "", Collections.<Integer, String>emptyMap());
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        simpleJobExecutor.execute();
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItemParameters().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenRunOnceAndThrowException() throws JobExecutionEnvironmentException {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        doThrow(RuntimeException.class).when(jobCaller).execute();
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobFacade).checkJobExecutionEnvironment();
            verify(jobFacade).getShardingContext();
            verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItemParameters().keySet());
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
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        simpleJobExecutor.execute();
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).execute();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isEligibleForJobRunning()).thenReturn(false);
        simpleJobExecutor.execute();
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
        verify(jobCaller).execute();
        verify(jobFacade, times(0)).clearMisfire(shardingContext.getShardingItemParameters().keySet());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfire() throws JobExecutionEnvironmentException {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())).thenReturn(true, false);
        when(jobFacade.isNeedSharding()).thenReturn(false);
        simpleJobExecutor.execute();
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContext();
        verify(jobFacade).misfireIfNecessary(shardingContext.getShardingItemParameters().keySet());
        verify(jobFacade, times(2)).registerJobBegin(shardingContext);
        verify(jobCaller, times(2)).execute();
        verify(jobFacade, times(2)).registerJobCompleted(shardingContext);
    }
    
    @Test(expected = JobSystemException.class)
    public void assertBeforeJobExecutedFailure() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContext);
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test(expected = JobSystemException.class)
    public void assertAfterJobExecutedFailure() {
        ShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        when(jobFacade.misfireIfNecessary(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContext.getShardingItemParameters().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContext);
        try {
            simpleJobExecutor.execute();
        } finally {
            verify(jobCaller).execute();
        }
    }
}
