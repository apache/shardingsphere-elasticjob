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

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.context.TaskContext.MetaInfo;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.netflix.fenzo.SchedulingResult;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskRequest;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action2;
import com.netflix.fenzo.plugins.VMLeaseObject;
import org.apache.mesos.SchedulerDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TaskLaunchScheduledServiceTest {
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private TaskScheduler taskScheduler;
    
    @Mock
    private FacadeService facadeService;
    
    @Mock
    private JobEventBus jobEventBus;
    
    private TaskLaunchScheduledService taskLaunchScheduledService;
    
    @Before
    public void setUp() throws Exception {
        when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app")));
        taskLaunchScheduledService = new TaskLaunchScheduledService(schedulerDriver, taskScheduler, facadeService, jobEventBus);
        taskLaunchScheduledService.startUp();
    }
    
    @After
    public void tearDown() throws Exception {
        taskLaunchScheduledService.shutDown();
    }
    
    @Test
    public void assertRunOneIteration() throws Exception {
        when(facadeService.getEligibleJobContext()).thenReturn(Lists.newArrayList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job", CloudJobExecutionType.DAEMON, 1), ExecutionType.FAILOVER)));
        Map<String, VMAssignmentResult> vmAssignmentResultMap = new HashMap<>();
        vmAssignmentResultMap.put("rs1", new VMAssignmentResult("localhost", Lists.<VirtualMachineLease>newArrayList(new VMLeaseObject(OfferBuilder.createOffer("offer_0"))),
                Sets.newHashSet(mockTaskAssignmentResult("failover_job", ExecutionType.FAILOVER))));
        when(taskScheduler.scheduleOnce(anyListOf(TaskRequest.class), anyListOf(VirtualMachineLease.class))).thenReturn(new SchedulingResult(vmAssignmentResultMap));
        when(facadeService.load("failover_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job")));
        when(facadeService.getFailoverTaskId(any(MetaInfo.class))).thenReturn(Optional.of(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", "failover_job",  ExecutionType.FAILOVER.name())));
        when(taskScheduler.getTaskAssigner()).thenReturn(mock(Action2.class));
        taskLaunchScheduledService.runOneIteration();
        verify(facadeService).removeLaunchTasksFromQueue(anyListOf(TaskContext.class));
        verify(facadeService).loadAppConfig("test_app");
        verify(jobEventBus).post(Matchers.<JobStatusTraceEvent>any());
    }
    
    @Test
    public void assertRunOneIterationWithScriptJob() throws Exception {
        when(facadeService.getEligibleJobContext()).thenReturn(Lists.newArrayList(
                JobContext.from(CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("script_job", 1), ExecutionType.READY)));
        Map<String, VMAssignmentResult> vmAssignmentResultMap = new HashMap<>();
        vmAssignmentResultMap.put("rs1", new VMAssignmentResult("localhost", Lists.<VirtualMachineLease>newArrayList(new VMLeaseObject(OfferBuilder.createOffer("offer_0"))),
                Sets.newHashSet(mockTaskAssignmentResult("script_job", ExecutionType.READY))));
        when(taskScheduler.scheduleOnce(anyListOf(TaskRequest.class), anyListOf(VirtualMachineLease.class))).thenReturn(new SchedulingResult(vmAssignmentResultMap));
        when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app")));
        when(facadeService.load("script_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("script_job", 1)));
        when(taskScheduler.getTaskAssigner()).thenReturn(mock(Action2.class));
        taskLaunchScheduledService.runOneIteration();
        verify(facadeService).removeLaunchTasksFromQueue(anyListOf(TaskContext.class));
        verify(facadeService).isRunning(TaskContext.from(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", "script_job", ExecutionType.READY)));
        verify(facadeService).loadAppConfig("test_app");
        verify(jobEventBus).post(Matchers.<JobStatusTraceEvent>any());
    }
    
    private TaskAssignmentResult mockTaskAssignmentResult(final String taskName, final ExecutionType executionType) {
        TaskAssignmentResult result = mock(TaskAssignmentResult.class);
        TaskRequest taskRequest = new JobTaskRequest(
                new TaskContext(taskName, Lists.newArrayList(0), executionType, "unassigned-slave"), CloudJobConfigurationBuilder.createCloudJobConfiguration(taskName));
        when(result.getTaskId()).thenReturn(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", taskName, executionType.name()));
        when(result.getHostname()).thenReturn("localhost");
        when(result.getAssignedPorts()).thenReturn(Lists.newArrayList(1234));
        when(result.getRequest()).thenReturn(taskRequest);
        when(result.isSuccessful()).thenReturn(true);
        when(result.getFitness()).thenReturn(1.0);
        return result; 
    }
    
    @Test
    public void assertScheduler() throws Exception {
        assertThat(taskLaunchScheduledService.scheduler(), instanceOf(Scheduler.class));
    }
    
    @Test
    public void assertServiceName() throws Exception {
        assertThat(taskLaunchScheduledService.serviceName(), is("task-launch-processor"));
    }
}
