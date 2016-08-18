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

package com.dangdang.ddframe.job.cloud.state.failover;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.state.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.state.running.RunningService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FailoverServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    private FailoverService failoverService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        failoverService = new FailoverService(regCenter);
        ReflectionUtils.setFieldValue(failoverService, "configService", configService);
        ReflectionUtils.setFieldValue(failoverService, "runningService", runningService);
    }
    
    @Test
    public void assertAddWhenJobIsNotPresent() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenIsDaemonJob() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job", JobExecutionType.DAEMON)));
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.isExisted("/state/failover/test_job/"  + taskNode.getTaskNodePath())).thenReturn(false);
        when(runningService.isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(runningService).isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsNotRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(configService.load("test_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job")));
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        when(runningService.isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(false);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(runningService).isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodeValue()));
        verify(regCenter).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        when(regCenter.isExisted("/state/failover")).thenReturn(false);
        assertTrue(failoverService.getAllEligibleJobContexts().isEmpty());
        verify(regCenter).isExisted("/state/failover");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        when(regCenter.isExisted("/state/failover")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/failover")).thenReturn(Arrays.asList("task_empty_job", "not_existed_job", "eligible_job"));
        when(regCenter.getChildrenKeys("/state/failover/task_empty_job")).thenReturn(Collections.<String>emptyList());
        when(regCenter.getChildrenKeys("/state/failover/not_existed_job")).thenReturn(Arrays.asList(
                TaskNode.builder().jobName("not_existed_job").build().getTaskNodePath(), TaskNode.builder().jobName("not_existed_job").shardingItem(1).build().getTaskNodePath()));
        String eligibleJobNodePath1 = TaskNode.builder().jobName("eligible_job").build().getTaskNodePath();
        String eligibleJobNodePath2 = TaskNode.builder().jobName("eligible_job").shardingItem(1).build().getTaskNodePath();
        when(regCenter.getChildrenKeys("/state/failover/eligible_job")).thenReturn(Arrays.asList(eligibleJobNodePath1, eligibleJobNodePath2));
        when(configService.load("not_existed_job")).thenReturn(Optional.<CloudJobConfiguration>absent());
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isTaskRunning(TaskContext.MetaInfo.from(eligibleJobNodePath1))).thenReturn(true);
        when(runningService.isTaskRunning(TaskContext.MetaInfo.from(eligibleJobNodePath2))).thenReturn(false);
        Collection<JobContext> actual = failoverService.getAllEligibleJobContexts();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getAssignedShardingItems().size(), is(1));
        assertThat(actual.iterator().next().getAssignedShardingItems().get(0), is(1));
        verify(regCenter).isExisted("/state/failover");
        verify(regCenter).remove("/state/failover/task_empty_job");
        verify(regCenter).remove("/state/failover/not_existed_job");
    }
    
    @Test
    public void assertRemove() {
        String jobNodePath1 = TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodePath();
        String jobNodePath2 = TaskNode.builder().shardingItem(1).type(ExecutionType.FAILOVER).build().getTaskNodePath();
        failoverService.remove(Arrays.asList(TaskContext.MetaInfo.from(jobNodePath1), TaskContext.MetaInfo.from(jobNodePath2)));
        verify(regCenter).remove("/state/failover/test_job/" + jobNodePath1);
        verify(regCenter).remove("/state/failover/test_job/" + jobNodePath2);
    }
}
