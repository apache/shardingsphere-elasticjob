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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.failover;

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.infra.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext.MetaInfo;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
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
    private CloudJobConfigurationService configService;
    
    @Mock
    private RunningService runningService;
    
    private FailoverService failoverService;
    
    @Before
    public void setUp() {
        failoverService = new FailoverService(regCenter);
        ReflectionUtils.setFieldValue(failoverService, "configService", configService);
        ReflectionUtils.setFieldValue(failoverService, "runningService", runningService);
    }
    
    @Test
    public void assertAddWhenJobIsOverQueueSize() {
        when(regCenter.getNumChildren(FailoverNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        when(runningService.isTaskRunning(MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(runningService).isTaskRunning(MetaInfo.from(taskNode.getTaskNodePath()));
        verify(regCenter, times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsNotRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        when(runningService.isTaskRunning(MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(false);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        verify(runningService).isTaskRunning(MetaInfo.from(taskNode.getTaskNodePath()));
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
        when(regCenter.getChildrenKeys("/state/failover/task_empty_job")).thenReturn(Collections.emptyList());
        when(regCenter.getChildrenKeys("/state/failover/not_existed_job")).thenReturn(Arrays.asList(
                TaskNode.builder().jobName("not_existed_job").build().getTaskNodePath(), TaskNode.builder().jobName("not_existed_job").shardingItem(1).build().getTaskNodePath()));
        String eligibleJobNodePath1 = TaskNode.builder().jobName("eligible_job").build().getTaskNodePath();
        String eligibleJobNodePath2 = TaskNode.builder().jobName("eligible_job").shardingItem(1).build().getTaskNodePath();
        when(regCenter.getChildrenKeys("/state/failover/eligible_job")).thenReturn(Arrays.asList(eligibleJobNodePath1, eligibleJobNodePath2));
        when(configService.load("not_existed_job")).thenReturn(Optional.empty());
        when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        when(runningService.isTaskRunning(MetaInfo.from(eligibleJobNodePath1))).thenReturn(true);
        when(runningService.isTaskRunning(MetaInfo.from(eligibleJobNodePath2))).thenReturn(false);
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
        failoverService.remove(Arrays.asList(MetaInfo.from(jobNodePath1), MetaInfo.from(jobNodePath2)));
        verify(regCenter).remove("/state/failover/test_job/" + jobNodePath1);
        verify(regCenter).remove("/state/failover/test_job/" + jobNodePath2);
    }
    
    @Test
    public void assertGetTaskId() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        when(regCenter.get("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(taskNode.getTaskNodeValue());
        assertThat(failoverService.getTaskId(taskNode.getMetaInfo()).get(), is(taskNode.getTaskNodeValue()));
        verify(regCenter, times(2)).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
    }
    
    @Test
    public void assertGetAllFailoverTasksWithoutRootNode() {
        when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(false);
        assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        verify(regCenter).isExisted(FailoverNode.ROOT);
    }
    
    @Test
    public void assertGetAllFailoverTasksWhenRootNodeHasNoChild() {
        when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Collections.emptyList());
        assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        verify(regCenter).isExisted(FailoverNode.ROOT);
        verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
    }
    
    @Test
    public void assertGetAllFailoverTasksWhenJobNodeHasNoChild() {
        when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Collections.singletonList("test_job"));
        when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job"))).thenReturn(Collections.emptyList());
        assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        verify(regCenter).isExisted(FailoverNode.ROOT);
        verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
        verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job"));
    }
    
    @Test
    public void assertGetAllFailoverTasksWithRootNode() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String uuid3 = UUID.randomUUID().toString();
        when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Arrays.asList("test_job_1", "test_job_2"));
        when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_1"))).thenReturn(Arrays.asList("test_job_1@-@0", "test_job_1@-@1"));
        when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_2"))).thenReturn(Collections.singletonList("test_job_2@-@0"));
        when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@0"))).thenReturn(uuid1);
        when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@1"))).thenReturn(uuid2);
        when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_2@-@0"))).thenReturn(uuid3);
        Map<String, Collection<FailoverTaskInfo>> result = failoverService.getAllFailoverTasks();
        assertThat(result.size(), is(2));
        assertThat(result.get("test_job_1").size(), is(2));
        assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[0].getTaskInfo().toString(), is("test_job_1@-@0"));
        assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[0].getOriginalTaskId(), is(uuid1));
        assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[1].getTaskInfo().toString(), is("test_job_1@-@1"));
        assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[1].getOriginalTaskId(), is(uuid2));
        assertThat(result.get("test_job_2").size(), is(1));
        assertThat(result.get("test_job_2").iterator().next().getTaskInfo().toString(), is("test_job_2@-@0"));
        assertThat(result.get("test_job_2").iterator().next().getOriginalTaskId(), is(uuid3));
        verify(regCenter).isExisted(FailoverNode.ROOT);
        verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
        verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_1"));
        verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_2"));
        verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@0"));
        verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@1"));
        verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_2@-@0"));
    }
}
