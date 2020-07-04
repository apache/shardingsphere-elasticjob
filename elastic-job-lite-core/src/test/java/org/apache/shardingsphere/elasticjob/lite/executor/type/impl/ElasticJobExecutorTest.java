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

package org.apache.shardingsphere.elasticjob.lite.executor.type.impl;

import org.apache.shardingsphere.elasticjob.lite.api.job.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.executor.type.fixture.job.FooJob;
import org.apache.shardingsphere.elasticjob.lite.fixture.ShardingContextsBuilder;
import org.apache.shardingsphere.elasticjob.lite.executor.type.fixture.executor.TestJobItemExecutor;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ElasticJobExecutorTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private FooJob fooJob;
    
    private JobConfiguration jobConfig;
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private TestJobItemExecutor jobItemExecutor;
    
    private ElasticJobExecutor elasticJobExecutor;
    
    @Before
    public void setUp() {
        jobConfig = createJobConfiguration();
        elasticJobExecutor = new ElasticJobExecutor(regCenter, fooJob, jobConfig, Collections.emptyList(), null);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobFacade", jobFacade);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobItemExecutor", jobItemExecutor);
    }
    
    private JobConfiguration createJobConfiguration() {
        return JobConfiguration.newBuilder(ShardingContextsBuilder.JOB_NAME, 3)
                .cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").jobParameter("param").failover(true).misfire(false).jobErrorHandlerType("THROW").description("desc").build();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() throws JobExecutionEnvironmentException {
        doThrow(JobExecutionEnvironmentException.class).when(jobFacade).checkJobExecutionEnvironment();
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        }
    }
    
    @Test
    public void assertExecuteWhenPreviousJobStillRunning() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", ShardingContextsBuilder.JOB_NAME, 3, "", Collections.emptyMap());
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, 
                "Previous job 'test_job' - shardingItems '[]' is still running, misfired job will start after previous job completed.");
        verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
    }
    
    @Test
    public void assertExecuteWhenShardingItemsIsEmpty() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 3, "", Collections.emptyMap());
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "Sharding item for job 'test_job' is empty.");
        verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteFailureWhenThrowExceptionForSingleShardingItem() {
        assertExecuteFailureWhenThrowException(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteFailureWhenThrowExceptionForMultipleShardingItems() {
        assertExecuteFailureWhenThrowException(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    private void assertExecuteFailureWhenThrowException(final ShardingContexts shardingContexts) {
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        doThrow(RuntimeException.class).when(jobItemExecutor).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_ERROR, getErrorMessage(shardingContexts));
            verify(jobFacade).registerJobBegin(shardingContexts);
            verify(jobItemExecutor, times(shardingContexts.getShardingTotalCount())).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
            verify(jobFacade).registerJobCompleted(shardingContexts);
        }
    }
    
    private String getErrorMessage(final ShardingContexts shardingContexts) {
        return 1 == shardingContexts.getShardingItemParameters().size()
                ? "{0=java.lang.RuntimeException" + System.lineSeparator() + "}"
                : "{0=java.lang.RuntimeException" + System.lineSeparator() + ", 1=java.lang.RuntimeException" + System.lineSeparator() + "}";
    }
    
    @Test
    public void assertExecuteSuccessForSingleShardingItems() {
        assertExecuteSuccess(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteSuccessForMultipleShardingItems() {
        assertExecuteSuccess(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    private void assertExecuteSuccess(final ShardingContexts shardingContexts) {
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "");
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(shardingContexts.getShardingTotalCount())).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
    }
    
    @Test
    public void assertExecuteWithMisfireIsEmpty() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        elasticJobExecutor.execute();
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
    }
    
    @Test
    public void assertExecuteWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        elasticJobExecutor.execute();
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        verify(jobFacade, times(0)).clearMisfire(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    public void assertExecuteWithMisfire() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true, false);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade, times(2)).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobFacade, times(2)).registerJobBegin(shardingContexts);
        verify(jobItemExecutor, times(4)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        verify(jobFacade, times(2)).registerJobCompleted(shardingContexts);
    }
    
    @Test(expected = JobSystemException.class)
    public void assertBeforeJobExecutedFailure() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContexts);
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        }
    }
    
    @Test(expected = JobSystemException.class)
    public void assertAfterJobExecutedFailure() {
        ShardingContexts shardingContexts = ShardingContextsBuilder.getMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContexts);
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobFacade), any());
        }
    }
    
    private void prepareForIsNotMisfire(final JobFacade jobFacade, final ShardingContexts shardingContexts) {
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
    }
    
    private void verifyForIsNotMisfire(final JobFacade jobFacade, final ShardingContexts shardingContexts) {
        try {
            verify(jobFacade).checkJobExecutionEnvironment();
        } catch (final JobExecutionEnvironmentException ex) {
            throw new RuntimeException(ex);
        }
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).beforeJobExecuted(shardingContexts);
        verify(jobFacade).registerJobBegin(shardingContexts);
        verify(jobFacade).registerJobCompleted(shardingContexts);
        verify(jobFacade).afterJobExecuted(shardingContexts);
    }
}
