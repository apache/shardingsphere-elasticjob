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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.cloud.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.FacadeService;
import com.dangdang.ddframe.job.cloud.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.state.fixture.TaskNode;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchedulerEngineTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private FacadeService facadeService;
    
    private SchedulerEngine schedulerEngine;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        schedulerEngine = new SchedulerEngine(regCenter);
        ReflectionUtils.setFieldValue(schedulerEngine, "facadeService", facadeService);
    }
    
    @Test
    public void assertRegistered() {
        schedulerEngine.registered(null, null, null);
        verify(facadeService).start();
    }
    
    @Test
    public void assertReregistered() {
        schedulerEngine.reregistered(null, null);
        verify(facadeService).start();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertResourceOffers() {
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        List<Protos.Offer> offers = Arrays.asList(OfferBuilder.createOffer("offer_0", 100d, 128000d), OfferBuilder.createOffer("offer_1", 100d, 128000d));
        when(facadeService.getEligibleJobContext()).thenReturn(
                Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER)));
        schedulerEngine.resourceOffers(schedulerDriver, offers);
        verify(schedulerDriver, times(0)).declineOffer(Protos.OfferID.newBuilder().setValue("offer_0").build());
        verify(schedulerDriver).declineOffer(Protos.OfferID.newBuilder().setValue("offer_1").build());
        verify(schedulerDriver).launchTasks(eq(Collections.singletonList(offers.get(0).getId())), (Collection) any());
        verify(facadeService, times(10)).addRunning((TaskContext) any());
        verify(facadeService).removeLaunchTasksFromQueue((List<TaskContext>) any());
    }
    
    @Test
    public void assertOfferRescinded() {
        schedulerEngine.offerRescinded(null, null);
    }
    
    @Test
    public void assertFinishedStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
    }
    
    @Test
    public void assertKilledStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_KILLED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        verify(facadeService).addDaemonJobToReadyQueue("test_job");
    }
    
    @Test
    public void assertFailedStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_FAILED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        verify(facadeService).addDaemonJobToReadyQueue("test_job");
    }
    
    @Test
    public void assertErrorStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder().setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue()))
                .setState(Protos.TaskState.TASK_ERROR).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        verify(facadeService).addDaemonJobToReadyQueue("test_job");
    }
    
    @Test
    public void assertLostStatusUpdate() {
        TaskNode taskNode = TaskNode.builder().build();
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskNode.getTaskNodeValue())).setState(Protos.TaskState.TASK_LOST).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(facadeService).removeRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        verify(facadeService).addDaemonJobToReadyQueue("test_job");
    }
    
    @Test
    public void assertFrameworkMessage() {
        schedulerEngine.frameworkMessage(null, null, Protos.SlaveID.newBuilder().setValue("slave-S0").build(), new byte[1]);
    }
    
    @Test
    public void assertDisconnected() {
        schedulerEngine.disconnected(null);
        verify(facadeService).stop();
    }
    
    @Test
    public void assertSlaveLost() {
        schedulerEngine.slaveLost(null, Protos.SlaveID.newBuilder().setValue("slave-S0").build());
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
