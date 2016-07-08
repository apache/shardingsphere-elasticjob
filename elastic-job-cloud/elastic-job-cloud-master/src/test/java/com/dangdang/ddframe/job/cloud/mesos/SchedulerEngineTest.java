/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.TaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.AssignedTaskContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.EligibleJobContext;
import com.dangdang.ddframe.job.cloud.mesos.facade.FacadeService;
import com.dangdang.ddframe.job.cloud.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
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
    
    @Test
    public void assertResourceOffers() {
        SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
        List<Protos.Offer> offers = Arrays.asList(OfferBuilder.createOffer("offer_0", 100d, 128000d), OfferBuilder.createOffer("offer_1", 100d, 128000d));
        EligibleJobContext eligibleJobContext = new EligibleJobContext(Collections.singletonList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"))), Collections.<String, JobContext>emptyMap(), Collections.<String, JobContext>emptyMap());
        when(facadeService.getEligibleJobContext()).thenReturn(eligibleJobContext);
        schedulerEngine.resourceOffers(schedulerDriver, offers);
        verify(schedulerDriver, times(0)).declineOffer(Protos.OfferID.newBuilder().setValue("offer_0").build());
        verify(schedulerDriver).declineOffer(Protos.OfferID.newBuilder().setValue("offer_1").build());
        verify(schedulerDriver).launchTasks(eq(Collections.singletonList(offers.get(0).getId())), (Collection) any());
        verify(facadeService).removeLaunchTasksFromQueue((AssignedTaskContext) any());
    }
    
    @Test
    public void assertOfferRescinded() {
        schedulerEngine.offerRescinded(null, null);
    }
    
    @Test
    public void assertStartingStatusUpdate() {
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue("test_job@-@0@-@00")).setState(Protos.TaskState.TASK_STARTING).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).addRunning("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertFinishedStatusUpdate() {
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue("test_job@-@0@-@00")).setState(Protos.TaskState.TASK_FINISHED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertFailedStatusUpdate() {
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue("test_job@-@0@-@00")).setState(Protos.TaskState.TASK_FAILED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertKilledStatusUpdate() {
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue("test_job@-@0@-@00")).setState(Protos.TaskState.TASK_KILLED).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertLostStatusUpdate() {
        schedulerEngine.statusUpdate(null, Protos.TaskStatus.newBuilder()
                .setTaskId(Protos.TaskID.newBuilder().setValue("test_job@-@0@-@00")).setState(Protos.TaskState.TASK_LOST).setSlaveId(Protos.SlaveID.newBuilder().setValue("slave-S0")).build());
        verify(facadeService).removeRunning("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertFrameworkMessage() {
        schedulerEngine.frameworkMessage(null, null, null, null);
    }
    
    @Test
    public void assertDisconnected() {
        schedulerEngine.disconnected(null);
        verify(facadeService).stop();
    }
    
    @Test
    public void assertSlaveLost() {
        schedulerEngine.slaveLost(null, Protos.SlaveID.newBuilder().setValue("slave-S0").build());
        verify(facadeService).recordFailoverTasks("slave-S0");
    }
    
    @Test
    public void assertExecutorLost() {
        schedulerEngine.executorLost(null, Protos.ExecutorID.newBuilder().setValue("test_job@-@0@-@00").build(), Protos.SlaveID.newBuilder().setValue("slave-S0").build(), 0);
        verify(facadeService).recordFailoverTask("slave-S0", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertError() {
        schedulerEngine.error(null, null);
    }
}