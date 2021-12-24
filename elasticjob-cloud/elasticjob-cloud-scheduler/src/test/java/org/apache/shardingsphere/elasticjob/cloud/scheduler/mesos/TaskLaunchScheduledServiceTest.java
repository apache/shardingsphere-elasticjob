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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.netflix.fenzo.SchedulingResult;
import com.netflix.fenzo.TaskAssignmentResult;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VMAssignmentResult;
import com.netflix.fenzo.functions.Action2;
import com.netflix.fenzo.plugins.VMLeaseObject;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudAppConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.fixture.OfferBuilder;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private JobTracingEventBus jobTracingEventBus;
    
    private TaskLaunchScheduledService taskLaunchScheduledService;
    
    @Before
    public void setUp() {
        when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app")));
        taskLaunchScheduledService = new TaskLaunchScheduledService(schedulerDriver, taskScheduler, facadeService, jobTracingEventBus);
        taskLaunchScheduledService.startUp();
    }
    
    @After
    public void tearDown() {
        taskLaunchScheduledService.shutDown();
    }
    
    @Test
    public void assertRunOneIteration() {
        when(facadeService.getEligibleJobContext()).thenReturn(
                Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job", CloudJobExecutionType.DAEMON, 1), ExecutionType.FAILOVER)));
        Map<String, VMAssignmentResult> vmAssignmentResultMap = new HashMap<>();
        vmAssignmentResultMap.put("rs1", new VMAssignmentResult("localhost", Collections.singletonList(new VMLeaseObject(OfferBuilder.createOffer("offer_0"))),
                Sets.newHashSet(mockTaskAssignmentResult("failover_job", ExecutionType.FAILOVER))));
        when(taskScheduler.scheduleOnce(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(new SchedulingResult(vmAssignmentResultMap));
        when(facadeService.load("failover_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job")));
        when(facadeService.getFailoverTaskId(any(MetaInfo.class))).thenReturn(Optional.of(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", "failover_job", ExecutionType.FAILOVER.name())));
        when(taskScheduler.getTaskAssigner()).thenReturn(mock(Action2.class));
        taskLaunchScheduledService.runOneIteration();
        verify(facadeService).removeLaunchTasksFromQueue(ArgumentMatchers.anyList());
        verify(facadeService).loadAppConfig("test_app");
        verify(jobTracingEventBus).post(ArgumentMatchers.<JobStatusTraceEvent>any());
    }
    
    @Test
    public void assertRunOneIterationWithScriptJob() {
        when(facadeService.getEligibleJobContext()).thenReturn(
                Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("script_job", 1).toCloudJobConfiguration(), ExecutionType.READY)));
        Map<String, VMAssignmentResult> vmAssignmentResultMap = new HashMap<>();
        vmAssignmentResultMap.put("rs1", new VMAssignmentResult("localhost", Collections.singletonList(new VMLeaseObject(OfferBuilder.createOffer("offer_0"))),
                Sets.newHashSet(mockTaskAssignmentResult("script_job", ExecutionType.READY))));
        when(taskScheduler.scheduleOnce(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(new SchedulingResult(vmAssignmentResultMap));
        when(facadeService.loadAppConfig("test_app")).thenReturn(Optional.of(CloudAppConfigurationBuilder.createCloudAppConfiguration("test_app")));
        when(facadeService.load("script_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("script_job", 1)));
        when(taskScheduler.getTaskAssigner()).thenReturn(mock(Action2.class));
        taskLaunchScheduledService.runOneIteration();
        verify(facadeService).removeLaunchTasksFromQueue(ArgumentMatchers.anyList());
        verify(facadeService).isRunning(TaskContext.from(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", "script_job", ExecutionType.READY)));
        verify(facadeService).loadAppConfig("test_app");
        verify(jobTracingEventBus).post(ArgumentMatchers.<JobStatusTraceEvent>any());
    }
    
    private TaskAssignmentResult mockTaskAssignmentResult(final String taskName, final ExecutionType executionType) {
        TaskAssignmentResult result = mock(TaskAssignmentResult.class);
        when(result.getTaskId()).thenReturn(String.format("%s@-@0@-@%s@-@unassigned-slave@-@0", taskName, executionType.name()));
        return result; 
    }
    
    @Test
    public void assertScheduler() {
        assertThat(taskLaunchScheduledService.scheduler(), instanceOf(Scheduler.class));
    }
    
    @Test
    public void assertServiceName() {
        assertThat(taskLaunchScheduledService.serviceName(), is("task-launch-processor"));
    }
}
