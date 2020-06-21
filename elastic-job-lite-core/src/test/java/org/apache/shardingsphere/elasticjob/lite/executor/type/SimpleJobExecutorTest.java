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

package org.apache.shardingsphere.elasticjob.lite.executor.type;

import org.apache.shardingsphere.elasticjob.lite.event.type.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.threadpool.impl.DefaultJobExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.error.impl.LogErrorJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.lite.fixture.ShardingContextsBuilder;
import org.apache.shardingsphere.elasticjob.lite.fixture.config.TestSimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.JobCaller;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.TestSimpleJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SimpleJobExecutorTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private ElasticJobExecutor elasticJobExecutor;
    
    @Before
    public void setUp() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration(null, "THROW"));
        elasticJobExecutor = new ElasticJobExecutor(new TestSimpleJob(jobCaller), jobFacade, new SimpleJobExecutor());
    }
    
    @Test
    public void assertNewExecutorWithDefaultHandlers() throws NoSuchFieldException {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        elasticJobExecutor = new ElasticJobExecutor(new TestSimpleJob(jobCaller), jobFacade, new SimpleJobExecutor());
        assertThat(ReflectionUtils.getFieldValue(elasticJobExecutor, ElasticJobExecutor.class.getDeclaredField("executorService")), 
                instanceOf(new DefaultJobExecutorServiceHandler().createExecutorService("test_job").getClass()));
        assertThat(ReflectionUtils.getFieldValue(elasticJobExecutor, ElasticJobExecutor.class.getDeclaredField("jobExceptionHandler")),
                instanceOf(LogErrorJobExceptionHandler.class));
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() throws JobExecutionEnvironmentException {
        doThrow(JobExecutionEnvironmentException.class).when(jobFacade).checkJobExecutionEnvironment();
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobFacade).checkJobExecutionEnvironment();
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() throws JobExecutionEnvironmentException {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, 
                "Previous job 'test_job' - shardingItems '[]' is still running, misfired job will start after previous job completed.");
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContexts();
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() throws JobExecutionEnvironmentException {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", Collections.emptyMap());
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "Sharding item for job 'test_job' is empty.");
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContexts();
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobCaller, times(0)).execute();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenRunOnceAndThrowExceptionForSingleShardingItem() throws JobExecutionEnvironmentException {
        assertExecuteWhenRunOnceAndThrowException(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteWhenRunOnceAndThrowExceptionForMultipleShardingItems() throws JobExecutionEnvironmentException {
        assertExecuteWhenRunOnceAndThrowException(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    private void assertExecuteWhenRunOnceAndThrowException(final ShardingContexts shardingContexts) throws JobExecutionEnvironmentException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        doThrow(RuntimeException.class).when(jobCaller).execute();
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
            String errorMessage;
            String lineSeparator = System.getProperty("line.separator");
            if (1 == shardingContexts.getShardingItemParameters().size()) {
                errorMessage = "{0=java.lang.RuntimeException" + lineSeparator + "}";
            } else {
                errorMessage = "{0=java.lang.RuntimeException" + lineSeparator + ", 1=java.lang.RuntimeException" + lineSeparator + "}";
            }
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_ERROR, errorMessage);
            verify(jobFacade).checkJobExecutionEnvironment();
            verify(jobFacade).getShardingContexts();
            verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
            verify(jobFacade).registerJobBegin(shardingContexts);
            verify(jobCaller, times(shardingContexts.getShardingTotalCount())).execute();
            verify(jobFacade).registerJobCompleted(shardingContexts);
        }
    }
    
    @Test
    public void assertExecuteWhenRunOnceSuccessForSingleShardingItems() {
        assertExecuteWhenRunOnceSuccess(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteWhenRunOnceSuccessForMultipleShardingItems() {
        assertExecuteWhenRunOnceSuccess(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    private void assertExecuteWhenRunOnceSuccess(final ShardingContexts shardingContexts) {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "");
        ElasticJobVerify.verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobCaller, times(shardingContexts.getShardingTotalCount())).execute();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsEmpty() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        elasticJobExecutor.execute();
        ElasticJobVerify.verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobCaller, times(2)).execute();
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        elasticJobExecutor.execute();
        ElasticJobVerify.verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobCaller, times(2)).execute();
        verify(jobFacade, times(0)).clearMisfire(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    public void assertExecuteWhenRunOnceWithMisfire() throws JobExecutionEnvironmentException {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true, false);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade, times(2)).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
        verify(jobFacade).checkJobExecutionEnvironment();
        verify(jobFacade).getShardingContexts();
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobFacade, times(2)).registerJobBegin(shardingContexts);
        verify(jobCaller, times(4)).execute();
        verify(jobFacade, times(2)).registerJobCompleted(shardingContexts);
    }
    
    @Test(expected = JobSystemException.class)
    public void assertBeforeJobExecutedFailure() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContexts);
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobCaller, times(0)).execute();
        }
    }
    
    @Test(expected = JobSystemException.class)
    public void assertAfterJobExecutedFailure() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContexts);
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobCaller, times(2)).execute();
        }
    }
}
