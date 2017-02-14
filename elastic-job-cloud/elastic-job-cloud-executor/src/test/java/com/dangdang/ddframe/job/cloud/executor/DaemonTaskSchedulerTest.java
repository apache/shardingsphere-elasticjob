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

package com.dangdang.ddframe.job.cloud.executor;

import com.dangdang.ddframe.job.cloud.executor.fixture.TestScriptJobConfiguration;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DaemonTaskSchedulerTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private ExecutorDriver executorDriver;
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    @Mock
    private AbstractElasticJobExecutor jobExecutor;
    
    @Mock
    private ShardingContexts shardingContexts;
    
    private TaskID taskId = TaskID.newBuilder().setValue(String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY)).build();
    
    private DaemonTaskScheduler.DaemonJob daemonJob;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        daemonJob = new DaemonTaskScheduler.DaemonJob();
        daemonJob.setElasticJob(null);
        daemonJob.setJobFacade(jobFacade);
        daemonJob.setExecutorDriver(executorDriver);
        daemonJob.setTaskId(taskId);
    }
    
    @Test
    public void assertJobRun() throws Exception {
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("test.sh"));
        daemonJob.execute(jobExecutionContext);
        verify(shardingContexts).setAllowSendJobEvent(true);
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("BEGIN").build());
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("COMPLETE").build());
        verify(shardingContexts).setCurrentJobEventSamplingCount(0);
    }
    
    @Test
    public void assertJobRunWithEventSampling() throws Exception {
        when(shardingContexts.getJobEventSamplingCount()).thenReturn(2);
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("test.sh"));
        daemonJob.execute(jobExecutionContext);
        verify(shardingContexts).setCurrentJobEventSamplingCount(1);
        verify(shardingContexts).setAllowSendJobEvent(false);
        when(shardingContexts.getCurrentJobEventSamplingCount()).thenReturn(1);
        daemonJob.execute(jobExecutionContext);
        verify(shardingContexts).setAllowSendJobEvent(true);
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("BEGIN").build());
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("COMPLETE").build());
        verify(shardingContexts).setCurrentJobEventSamplingCount(0);
    }
}
