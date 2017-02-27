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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.scheduler.ha.FrameworkIDService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.functions.Action2;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
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
    public void setUp() throws NoSuchFieldException {
        schedulerEngine = new SchedulerEngine(taskScheduler, facadeService, new JobEventBus(), frameworkIDService, statisticManager);
        ReflectionUtils.setFieldValue(schedulerEngine, "facadeService", facadeService);
        when(facadeService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        new RunningService(Mockito.mock(CoordinatorRegistryCenter.class)).clear();
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
        when(facadeService.getEligibleJobContext()).thenReturn(
                Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER)));
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
        verify(facadeService, times(0)).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), eq(anyBoolean()));
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
