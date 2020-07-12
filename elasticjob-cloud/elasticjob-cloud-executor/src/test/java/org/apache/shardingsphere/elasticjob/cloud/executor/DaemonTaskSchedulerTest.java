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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DaemonTaskSchedulerTest {
    
    @Mock
    private CloudJobFacade jobFacade;
    
    @Mock
    private ExecutorDriver executorDriver;
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    @Mock
    private ShardingContexts shardingContexts;
    
    private TaskID taskId = TaskID.newBuilder().setValue(String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY)).build();
    
    private DaemonTaskScheduler.DaemonJob daemonJob;
    
    @Before
    public void setUp() {
        daemonJob = new DaemonTaskScheduler.DaemonJob();
        daemonJob.setJobFacade(jobFacade);
        daemonJob.setElasticJobType("SCRIPT");
        daemonJob.setExecutorDriver(executorDriver);
        daemonJob.setTaskId(taskId);
    }
    
    @Test
    public void assertJobRun() {
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.loadJobConfiguration(true)).thenReturn(createJobConfiguration());
        daemonJob.execute(jobExecutionContext);
        verify(shardingContexts).setAllowSendJobEvent(true);
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("BEGIN").build());
        verify(executorDriver).sendStatusUpdate(TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_RUNNING).setMessage("COMPLETE").build());
        verify(shardingContexts).setCurrentJobEventSamplingCount(0);
    }
    
    @Test
    public void assertJobRunWithEventSampling() {
        when(shardingContexts.getJobEventSamplingCount()).thenReturn(2);
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        when(jobFacade.loadJobConfiguration(true)).thenReturn(createJobConfiguration());
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
    
    private JobConfiguration createJobConfiguration() {
        return JobConfiguration.newBuilder("test_script_job", 3).cron("0/1 * * * * ?").jobErrorHandlerType("IGNORE")
                .setProperty(ScriptJobProperties.SCRIPT_KEY, "test.sh").build();
    }
}
