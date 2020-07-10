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

import com.google.common.base.Optional;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.functions.Action2;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.OfferBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

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
        Mockito.when(facadeService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        new RunningService(Mockito.mock(CoordinatorRegistryCenter.class)).clear();
    }
    
    @Test
    public void assertRegistered() {
        schedulerEngine.registered(null, Protos.FrameworkID.newBuilder().setValue("1").build(), Protos.MasterInfo.getDefaultInstance());
        Mockito.verify(taskScheduler).expireAllLeases();
        Mockito.verify(frameworkIDService).save("1");
    }
    
    @Test
    public void assertReregistered() {
        schedulerEngine.reregistered(null, Protos.MasterInfo.getDefaultInstance());
        Mockito.verify(taskScheduler).expireAllLeases();
    }
    
    @Test
    public void assertResourceOffers() {
        SchedulerDriver schedulerDriver = Mockito.mock(SchedulerDriver.class);
        List<Protos.Offer> offers = Arrays.asList(OfferBuilder.createOffer("offer_0"), OfferBuilder.createOffer("offer_1"));
        schedulerEngine.resourceOffers(schedulerDriver, offers);
        Assert.assertThat(LeasesQueue.getInstance().drainTo().size(), Is.is(2));
    }
    
    @Test
    public void assertOfferRescinded() {
        schedulerEngine.offerRescinded(null, Protos.OfferID.newBuilder().setValue("myOffer").build());
        Mockito.verify(taskScheduler).expireLease("myOffer");
    }
    
    @Test
    public void assertRunningStatusUpdateForDaemonJobBegin() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setMessage("BEGIN").setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), false);
    }
    
    @Test
    public void assertRunningStatusUpdateForDaemonJobComplete() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setMessage("COMPLETE").setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), true);
    }
    
    @Test
    public void assertRunningStatusUpdateForOther() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_RUNNING).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService, Mockito.times(0)).updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), ArgumentMatchers.eq(ArgumentMatchers.anyBoolean()));
    }
    
    @Test
    public void assertFinishedStatusUpdateWithoutLaunchedTasks() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskScheduler, Mockito.times(0)).getTaskUnAssigner();
    }
    
    @Test
    public void assertFinishedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunSuccessfully();
    }
    
    @Test
    public void assertKilledStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_KILLED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).addDaemonJobToReadyQueue("test_job");
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
    }
    
    @Test
    public void assertFailedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FAILED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertErrorStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_ERROR).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertLostStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_LOST).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertDroppedStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_DROPPED)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertGoneStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_GONE).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertGoneByOperatorStatusUpdate() {
        @SuppressWarnings("unchecked")
        Action2<String, String> taskUnAssigner = Mockito.mock(Action2.class);
        Mockito.when(taskScheduler.getTaskUnAssigner()).thenReturn(taskUnAssigner);
        TaskNode taskNode = TaskNode.builder().build();
        Mockito.when(facadeService.popMapping(taskNode.getTaskNodeValue())).thenReturn("localhost");
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_GONE_BY_OPERATOR)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(facadeService).removeRunning(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(taskUnAssigner).call(TaskContext.getIdForUnassignedSlave(taskNode.getTaskNodeValue()), "localhost");
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertUnknownStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_UNKNOWN)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertUnReachedStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_UNREACHABLE)
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        Mockito.verify(statisticManager).taskRunFailed();
    }
    
    @Test
    public void assertFrameworkMessage() {
        schedulerEngine.frameworkMessage(null, null, Protos.SlaveID.newBuilder().setValue("slave-S0").build(), new byte[1]);
    }
    
    @Test
    public void assertSlaveLost() {
        schedulerEngine.slaveLost(null, Protos.SlaveID.newBuilder().setValue("slave-S0").build());
        Mockito.verify(taskScheduler).expireAllLeasesByVMId("slave-S0");
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
