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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.functions.Action2;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.OfferBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchedulerEngineTest {
    
    @Mock
    private TaskScheduler taskScheduler;
    
    @Mock
    private FacadeService facadeService;
    
    @Mock
    private FrameworkIDService frameworkIDService;
    
    @Mock
    private StatisticManager statisticManager;
    
    private SchedulerEngine schedulerEngine;
    
    @Before
    public void setUp() {
        schedulerEngine = new SchedulerEngine(taskScheduler, facadeService, new JobTracingEventBus(), frameworkIDService, statisticManager);
        ReflectionUtils.setFieldValue(schedulerEngine, "facadeService", facadeService);
        when(facadeService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        new RunningService(mock(CoordinatorRegistryCenter.class)).clear();
    }
    
    @Test
    public void assertRegistered() {
        schedulerEngine.registered(null, Protos.FrameworkID.newBuilder().setValue("1").build(), Protos.MasterInfo.getDefaultInstance());
        verify(taskScheduler).expireAllLeases();
        verify(frameworkIDService).save("1");
    }
    
    @Test
    public void assertReregistered() {
        schedulerEngine.reregistered(null, Protos.MasterInfo.getDefaultInstance());
        verify(taskScheduler).expireAllLeases();
    }
    
    @Test
    public void assertResourceOffers() {
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        List<Protos.Offer> offers = Arrays.asList(OfferBuilder.createOffer("offer_0"), OfferBuilder.createOffer("offer_1"));
        schedulerEngine.resourceOffers(schedulerDriver, offers);
        assertThat(LeasesQueue.getInstance().drainTo().size(), is(2));
    }
    
    @Test
    public void assertOfferRescinded() {
        schedulerEngine.offerRescinded(null, Protos.OfferID.newBuilder().setValue("myOffer").build());
        verify(taskScheduler).expireLease("myOffer");
    }
    
    @Test
    public void assertRunningStatusUpdateForDaemonJobBegin() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setMessage("BEGIN").setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), false);
    }
    
    @Test
    public void assertRunningStatusUpdateForDaemonJobComplete() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setMessage("COMPLETE").setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), true);
    }
    
    @Test
    public void assertRunningStatusUpdateForOther() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService, times(0)).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), ArgumentMatchers.eq(ArgumentMatchers.anyBoolean()));
    }
    
    @Test
    public void assertFinishedStatusUpdateWithoutLaunchedTasks() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskScheduler, times(0)).getTaskUnAssigner();
    }
    
    @Test
    public void assertFinishedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunSuccessfully();
    }
    
    @Test
    public void assertKilledStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_KILLED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).addDaemonJobToReadyQueue("test_job");
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
    }
    
    @Test
    public void assertFailedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FAILED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertErrorStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_ERROR).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertLostStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_LOST).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertDroppedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_DROPPED)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertGoneStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_GONE).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertGoneByOperatorStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = mock(Action2.class);
        when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_GONE_BY_OPERATOR)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertUnknownStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_UNKNOWN)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertUnReachedStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_UNREACHABLE)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertFrameworkMessage() {
        schedulerEngine.frameworkMessage(null, null, Protos.SlaveID.newBuilder().setValue("slave-S0").build(), new byte[1]);
    }
    
    @Test
    public void assertSlaveLost() {
        schedulerEngine.slaveLost(null, Protos.SlaveID.newBuilder().setValue("slave-S0").build());
        verify(taskScheduler).expireAllLeasesByVMId("slave-S0");
    }
    
    @Test
    public void assertExecutorLost() {
        schedulerEngine.executorLost(null, Protos.ExecutorID.newBuilder().setValue("test_job@-@0@-@00").build(), Protos.SlaveID.newBuilder().setValue("slave-S0").build(), 0);
    }
    
    @Test
    public void assertError() {
        schedulerEngine.error(null, null);
    }
}
