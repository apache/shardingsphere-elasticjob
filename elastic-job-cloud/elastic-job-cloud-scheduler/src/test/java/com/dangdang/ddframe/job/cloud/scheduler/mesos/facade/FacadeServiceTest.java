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

package com.dangdang.ddframe.job.cloud.scheduler.mesos.facade;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.producer.TaskProducerSchedulerRegistry;
import com.dangdang.ddframe.job.cloud.scheduler.state.failover.FailoverService;
import com.dangdang.ddframe.job.cloud.scheduler.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.scheduler.state.misfired.MisfiredService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FacadeServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private MisfiredService misfiredService;
    
    @Mock
    private TaskProducerSchedulerRegistry taskProducerSchedulerRegistry;
    
    private FacadeService facadeService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        facadeService = new FacadeService(regCenter);
        ReflectionUtils.setFieldValue(facadeService, "configService", configService);
        ReflectionUtils.setFieldValue(facadeService, "readyService", readyService);
        ReflectionUtils.setFieldValue(facadeService, "runningService", runningService);
        ReflectionUtils.setFieldValue(facadeService, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(facadeService, "misfiredService", misfiredService);
        ReflectionUtils.setFieldValue(facadeService, "taskProducerSchedulerRegistry", taskProducerSchedulerRegistry);
    }
    
    @Test
    public void assertStart() {
        facadeService.start();
        verify(runningService).clear();
        verify(taskProducerSchedulerRegistry).startup();
    }
    
    @Test
    public void assertGetEligibleJobContext() {
        Collection<JobContext> failoverJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER));
        Collection<JobContext> misfiredJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("misfire_job"), ExecutionType.MISFIRED));
        Collection<JobContext> readyJobContexts = Collections.singletonList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ready_job"), ExecutionType.READY));
        when(failoverService.getAllEligibleJobContexts()).thenReturn(failoverJobContexts);
        when(misfiredService.getAllEligibleJobContexts(failoverJobContexts)).thenReturn(misfiredJobContexts);
        when(readyService.getAllEligibleJobContexts(Arrays.asList(failoverJobContexts.iterator().next(), misfiredJobContexts.iterator().next()))).thenReturn(readyJobContexts);
        Collection<JobContext> actual = facadeService.getEligibleJobContext();
        assertThat(actual.size(), is(3));
        int i = 0;
        for (JobContext each : actual) {
            switch (i) {
                case 0:
                    assertThat(each.getJobConfig().getJobName(), is("failover_job"));
                    break;
                case 1:
                    assertThat(each.getJobConfig().getJobName(), is("misfire_job"));
                    break;
                case 2:
                    assertThat(each.getJobConfig().getJobName(), is("ready_job"));
                    break;
                default:
                    break;
            }
            i++;
        }
    }
    
    @Test
    public void assertRemoveLaunchTasksFromQueue() {
        facadeService.removeLaunchTasksFromQueue(Arrays.asList(
                TaskContext.from(TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodeValue()), 
                TaskContext.from(TaskNode.builder().type(ExecutionType.MISFIRED).build().getTaskNodeValue()), 
                TaskContext.from(TaskNode.builder().build().getTaskNodeValue())));
        verify(failoverService).remove(Collections.singletonList(TaskContext.MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
        verify(misfiredService).remove(Sets.newHashSet("test_job"));
        verify(readyService).remove(Sets.newHashSet("test_job"));
    }
    
    @Test
    public void assertAddRunning() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.addRunning(taskContext);
        verify(runningService).add(taskContext);
    }
    
    @Test
    public void assertUpdateDaemonStatus() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        facadeService.updateDaemonStatus(taskContext, true);
        verify(runningService).updateDaemonStatus(taskContext, true);
    }
    
    @Test
    public void assertRemoveRunning() {
        String taskNodePath = TaskNode.builder().build().getTaskNodePath();
        TaskContext.MetaInfo metaInfo = TaskContext.MetaInfo.from(taskNodePath);
        facadeService.removeRunning(metaInfo);
        verify(runningService).remove(metaInfo);
    }
    
    @Test
    public void assertRecordFailoverTaskWhenJobConfigNotExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(failoverService, times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(runningService).remove(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverDisabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createOtherCloudJobConfiguration("test_job")));
        facadeService.recordFailoverTask(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(failoverService, times(0)).add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(runningService).remove(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
    }
    
    @Test
    public void assertRecordFailoverTaskWhenIsFailoverEnabled() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        TaskContext taskContext = TaskContext.from(taskNode.getTaskNodeValue());
        facadeService.recordFailoverTask(taskContext);
        verify(failoverService).add(taskContext);
        verify(runningService).remove(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
    }
    
    @Test
    public void assertLoad() {
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        assertThat(facadeService.load("test_job"), is(Optional.<CloudJobConfiguration>absent()));
    }
    
    @Test
    public void assertAddDaemonJobToReadyQueue() {
        facadeService.addDaemonJobToReadyQueue("test_job");
        verify(readyService).addDaemon("test_job");
    }
    
    @Test
    public void assertGetRunningTasks() {
        when(runningService.getRunningTasks("test_job")).thenReturn(Collections.<TaskContext>emptyList());
        assertThat(facadeService.getRunningTasks("test_job"), Is.<Collection<TaskContext>>is(Collections.<TaskContext>emptyList()));
    }
    
    @Test
    public void assertStop() {
        facadeService.stop();
        verify(runningService).clear();
    }
}
