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

package org.apache.shardingsphere.elasticjob.kernel.internal.executor;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.kernel.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.kernel.fixture.executor.ClassedFooJobExecutor;
import org.apache.shardingsphere.elasticjob.kernel.fixture.job.FooJob;
import org.apache.shardingsphere.elasticjob.spi.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.kernel.internal.tracing.event.JobStatusTraceEvent.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ElasticJobExecutorTest {
    
    @Mock
    private FooJob fooJob;
    
    private JobConfiguration jobConfig;
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private JobRuntimeService jobRuntimeService;
    
    @Mock
    private ClassedFooJobExecutor jobItemExecutor;
    
    private ElasticJobExecutor elasticJobExecutor;
    
    @BeforeEach
    void setUp() {
        jobConfig = createJobConfiguration();
        when(jobFacade.loadJobConfiguration(anyBoolean())).thenReturn(jobConfig);
        when(jobFacade.getJobRuntimeService()).thenReturn(jobRuntimeService);
        elasticJobExecutor = new ElasticJobExecutor(fooJob, jobConfig, jobFacade);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobItemExecutor", jobItemExecutor);
    }
    
    private JobConfiguration createJobConfiguration() {
        return JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").jobParameter("param").failover(true).misfire(false).jobErrorHandlerType("THROW").description("desc").build();
    }
    
    @Test
    void assertExecuteWhenCheckMaxTimeDiffSecondsIntolerable() {
        assertThrows(JobSystemException.class, () -> {
            doThrow(JobExecutionEnvironmentException.class).when(jobFacade).checkJobExecutionEnvironment();
            try {
                elasticJobExecutor.execute();
            } finally {
                verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
            }
        });
    }
    
    @Test
    void assertExecuteWhenPreviousJobStillRunning() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 3, "", Collections.emptyMap());
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED,
                "Previous job 'test_job' - shardingItems '[]' is still running, misfired job will start after previous job completed.");
        verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
    }
    
    @Test
    void assertExecuteWhenShardingItemsIsEmpty() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 3, "", Collections.emptyMap());
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "Sharding item for job 'test_job' is empty.");
        verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
    }
    
    @Test
    void assertExecuteFailureWhenThrowExceptionForSingleShardingItem() {
        assertThrows(JobSystemException.class, () -> assertExecuteFailureWhenThrowException(createSingleShardingContexts()));
    }
    
    @Test
    void assertExecuteFailureWhenThrowExceptionForMultipleShardingItems() {
        assertExecuteFailureWhenThrowException(createMultipleShardingContexts());
    }
    
    private void assertExecuteFailureWhenThrowException(final ShardingContexts shardingContexts) {
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        doThrow(RuntimeException.class).when(jobItemExecutor).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
        try {
            elasticJobExecutor.execute();
        } finally {
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
            verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_ERROR, getErrorMessage(shardingContexts));
            verify(jobFacade).registerJobBegin(shardingContexts);
            verify(jobItemExecutor, times(shardingContexts.getShardingTotalCount())).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
            verify(jobFacade).registerJobCompleted(shardingContexts);
        }
    }
    
    private String getErrorMessage(final ShardingContexts shardingContexts) {
        return 1 == shardingContexts.getShardingItemParameters().size()
                ? "{0=java.lang.RuntimeException" + System.lineSeparator() + "}"
                : "{0=java.lang.RuntimeException" + System.lineSeparator() + ", 1=java.lang.RuntimeException" + System.lineSeparator() + "}";
    }
    
    @Test
    void assertExecuteSuccessForSingleShardingItems() {
        assertExecuteSuccess(createSingleShardingContexts());
    }
    
    @Test
    void assertExecuteSuccessForMultipleShardingItems() {
        assertExecuteSuccess(createMultipleShardingContexts());
    }
    
    private void assertExecuteSuccess(final ShardingContexts shardingContexts) {
        prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_FINISHED, "");
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(shardingContexts.getShardingTotalCount())).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
    }
    
    @Test
    void assertExecuteWithMisfireIsEmpty() {
        ShardingContexts shardingContexts = createMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        elasticJobExecutor.execute();
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
    }
    
    @Test
    void assertExecuteWithMisfireIsNotEmptyButIsNotEligibleForJobRunning() {
        ShardingContexts shardingContexts = createMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        elasticJobExecutor.execute();
        verifyForIsNotMisfire(jobFacade, shardingContexts);
        verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
        verify(jobFacade, times(0)).clearMisfire(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertExecuteWithMisfire() {
        ShardingContexts shardingContexts = createMultipleShardingContexts();
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(true, false);
        elasticJobExecutor.execute();
        verify(jobFacade).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_STAGING, "Job 'test_job' execute begin.");
        verify(jobFacade, times(2)).postJobStatusTraceEvent(shardingContexts.getTaskId(), State.TASK_RUNNING, "");
        verify(jobFacade).misfireIfRunning(shardingContexts.getShardingItemParameters().keySet());
        verify(jobFacade, times(2)).registerJobBegin(shardingContexts);
        verify(jobItemExecutor, times(4)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
        verify(jobFacade, times(2)).registerJobCompleted(shardingContexts);
    }
    
    @Test
    void assertBeforeJobExecutedFailure() {
        assertThrows(JobSystemException.class, () -> {
            ShardingContexts shardingContexts = createMultipleShardingContexts();
            when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
            doThrow(RuntimeException.class).when(jobFacade).beforeJobExecuted(shardingContexts);
            try {
                elasticJobExecutor.execute();
            } finally {
                verify(jobItemExecutor, times(0)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
            }
        });
    }
    
    @Test
    void assertAfterJobExecutedFailure() {
        assertThrows(JobSystemException.class, () -> {
            ShardingContexts shardingContexts = createMultipleShardingContexts();
            when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
            doThrow(RuntimeException.class).when(jobFacade).afterJobExecuted(shardingContexts);
            try {
                elasticJobExecutor.execute();
            } finally {
                verify(jobItemExecutor, times(2)).process(eq(fooJob), eq(jobConfig), eq(jobRuntimeService), any());
            }
        });
    }
    
    private ShardingContexts createSingleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        return new ShardingContexts("fake_task_id", "test_job", 1, "", map);
    }
    
    private ShardingContexts createMultipleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "A");
        map.put(1, "B");
        return new ShardingContexts("fake_task_id", "test_job", 2, "", map);
    }
    
    private void prepareForIsNotMisfire(final JobFacade jobFacade, final ShardingContexts shardingContexts) {
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.misfireIfRunning(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
        lenient().when(jobFacade.isExecuteMisfired(shardingContexts.getShardingItemParameters().keySet())).thenReturn(false);
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
