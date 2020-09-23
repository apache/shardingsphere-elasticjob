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

package org.apache.shardingsphere.elasticjob.cloud.executor.prod;

import lombok.SneakyThrows;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.prod.DaemonTaskScheduler.DaemonJob;
import org.apache.shardingsphere.elasticjob.cloud.facade.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private ElasticJob elasticJob;
    
    private final TaskID taskId = TaskID.newBuilder().setValue(String.format("%s@-@0@-@%s@-@fake_slave_id@-@0", "test_job", ExecutionType.READY)).build();
    
    private DaemonJob daemonJob;
    
    @Before
    public void setUp() {
        daemonJob = new DaemonJob();
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
        return JobConfiguration.newBuilder("test_script_job", 3).cron("0/1 * * * * ?").jobErrorHandlerType("IGNORE").setProperty(ScriptJobProperties.SCRIPT_KEY, "echo test").build();
    }
    
    @Test
    @SneakyThrows
    public void assertInit() {
        DaemonTaskScheduler scheduler = createScheduler();
        scheduler.init();
        Field field = DaemonTaskScheduler.class.getDeclaredField("RUNNING_SCHEDULERS");
        field.setAccessible(true);
        assertTrue(((ConcurrentHashMap) field.get(scheduler)).containsKey(taskId.getValue()));
        DaemonTaskScheduler.shutdown(taskId);
    }
    
    @Test
    @SneakyThrows
    public void assertShutdown() {
        DaemonTaskScheduler scheduler = createScheduler();
        scheduler.init();
        DaemonTaskScheduler.shutdown(taskId);
        Field field = DaemonTaskScheduler.class.getDeclaredField("RUNNING_SCHEDULERS");
        field.setAccessible(true);
        assertFalse(((ConcurrentHashMap) field.get(scheduler)).containsKey(taskId.getValue()));
        assertTrue(((ConcurrentHashMap) field.get(scheduler)).isEmpty());
    }
    
    private DaemonTaskScheduler createScheduler() {
        when(jobConfig.getJobName()).thenReturn(taskId.getValue());
        when(jobConfig.getCron()).thenReturn("0/1 * * * * ?");
        return new DaemonTaskScheduler(elasticJob, "transient", jobConfig, jobFacade, executorDriver, taskId);
    }
}
