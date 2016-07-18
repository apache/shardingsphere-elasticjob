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

package com.dangdang.ddframe.job.cloud.mesos.facade;

import com.dangdang.ddframe.job.cloud.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.mesos.stragety.ResourceAllocateStrategy;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EligibleJobContextTest {
    
    @Mock
    private ResourceAllocateStrategy resourceAllocateStrategy;
    
    @Test
    public void assertAllocate() {
        Collection<JobContext> failoverJobContext = Collections.singleton(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER));
        Collection<JobContext> misfiredJobContext = Collections.singleton(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("misfired_job"), ExecutionType.MISFIRED));
        Collection<JobContext> readyJobContext = Collections.singleton(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ready_job"), ExecutionType.READY));
        EligibleJobContext actual = new EligibleJobContext(failoverJobContext, misfiredJobContext, readyJobContext);
        when(resourceAllocateStrategy.allocate(failoverJobContext)).thenReturn(Collections.singletonList(Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder()
                        .setValue("failover_job@-@0@-@FAILOVER@-@00").build()).setName("failover_job@-@0@-@FAILOVER@-@00").setSlaveId(Protos.SlaveID.newBuilder().setValue("salve-S0").build()).build()));
        List<Protos.TaskInfo> taskInfoList = Collections.singletonList(Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder()
                .setValue("misfired_job@-@0@-@MISFIRED@-@00").build()).setName("misfired_job@-@0@-@MISFIRED@-@00").setSlaveId(Protos.SlaveID.newBuilder().setValue("salve-S0").build()).build());
        when(resourceAllocateStrategy.allocate(misfiredJobContext)).thenReturn(taskInfoList);
        List<Protos.TaskInfo> readyTasks = Collections.singletonList(Protos.TaskInfo.newBuilder().setTaskId(Protos.TaskID.newBuilder()
                .setValue("ready_job@-@0@-@READY@-@00").build()).setName("ready_job@-@0@-@READY@-@00").setSlaveId(Protos.SlaveID.newBuilder().setValue("salve-S0").build()).build());
        when(resourceAllocateStrategy.allocate(readyJobContext)).thenReturn(readyTasks);
        AssignedTaskContext assignedTaskContext = actual.allocate(resourceAllocateStrategy);
        assertThat(assignedTaskContext.getTaskInfoList().size(), is(3));
        assertThat(assignedTaskContext.getTaskInfoList().get(0).getTaskId().getValue(), is("failover_job@-@0@-@FAILOVER@-@00"));
        assertThat(assignedTaskContext.getTaskInfoList().get(1).getTaskId().getValue(), is("misfired_job@-@0@-@MISFIRED@-@00"));
        assertThat(assignedTaskContext.getTaskInfoList().get(2).getTaskId().getValue(), is("ready_job@-@0@-@READY@-@00"));
        assertThat(assignedTaskContext.getFailoverTaskContexts().size(), is(1));
        assertThat(assignedTaskContext.getFailoverTaskContexts().iterator().next().getJobName(), is("failover_job"));
        assertThat(assignedTaskContext.getMisfiredJobNames().size(), is(1));
        assertThat(assignedTaskContext.getMisfiredJobNames().iterator().next(), is("misfired_job"));
        assertThat(assignedTaskContext.getReadyJobNames().size(), is(1));
        assertThat(assignedTaskContext.getReadyJobNames().iterator().next(), is("ready_job"));
    }
}
