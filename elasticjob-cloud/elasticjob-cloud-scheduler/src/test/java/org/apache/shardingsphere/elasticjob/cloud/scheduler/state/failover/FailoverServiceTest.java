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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.context.JobContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public void setUp() throws NoSuchFieldException {
        failoverService = new FailoverService(regCenter);
        ReflectionUtils.setFieldValue(failoverService, "configService", configService);
        ReflectionUtils.setFieldValue(failoverService, "runningService", runningService);
    }
    
    @Test
    public void assertAddWhenJobIsOverQueueSize() {
        Mockito.when(regCenter.getNumChildren(FailoverNode.ROOT)).thenReturn(BootstrapEnvironment.getInstance().getFrameworkConfiguration().getJobStateQueueSize() + 1);
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenExisted() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        Mockito.when(runningService.isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(true);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        Mockito.verify(runningService).isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        Mockito.verify(regCenter, Mockito.times(0)).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWhenNotExistedAndTaskIsNotRunning() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        Mockito.when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        Mockito.when(runningService.isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()))).thenReturn(false);
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.verify(regCenter).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
        Mockito.verify(runningService).isTaskRunning(TaskContext.MetaInfo.from(taskNode.getTaskNodePath()));
        Mockito.verify(regCenter).persist("/state/failover/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithoutRootNode() {
        Mockito.when(regCenter.isExisted("/state/failover")).thenReturn(false);
        Assert.assertTrue(failoverService.getAllEligibleJobContexts().isEmpty());
        Mockito.verify(regCenter).isExisted("/state/failover");
    }
    
    @Test
    public void assertGetAllEligibleJobContextsWithRootNode() {
        Mockito.when(regCenter.isExisted("/state/failover")).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys("/state/failover")).thenReturn(Arrays.asList("task_empty_job", "not_existed_job", "eligible_job"));
        Mockito.when(regCenter.getChildrenKeys("/state/failover/task_empty_job")).thenReturn(Collections.emptyList());
        Mockito.when(regCenter.getChildrenKeys("/state/failover/not_existed_job")).thenReturn(Arrays.asList(
                TaskNode.builder().jobName("not_existed_job").build().getTaskNodePath(), TaskNode.builder().jobName("not_existed_job").shardingItem(1).build().getTaskNodePath()));
        String eligibleJobNodePath1 = TaskNode.builder().jobName("eligible_job").build().getTaskNodePath();
        String eligibleJobNodePath2 = TaskNode.builder().jobName("eligible_job").shardingItem(1).build().getTaskNodePath();
        Mockito.when(regCenter.getChildrenKeys("/state/failover/eligible_job")).thenReturn(Arrays.asList(eligibleJobNodePath1, eligibleJobNodePath2));
        Mockito.when(configService.load("not_existed_job")).thenReturn(Optional.empty());
        Mockito.when(configService.load("eligible_job")).thenReturn(Optional.of(CloudJobConfigurationBuilder.createCloudJobConfiguration("eligible_job")));
        Mockito.when(runningService.isTaskRunning(TaskContext.MetaInfo.from(eligibleJobNodePath1))).thenReturn(true);
        Mockito.when(runningService.isTaskRunning(TaskContext.MetaInfo.from(eligibleJobNodePath2))).thenReturn(false);
        Collection<JobContext> actual = failoverService.getAllEligibleJobContexts();
        Assert.assertThat(actual.size(), Is.is(1));
        Assert.assertThat(actual.iterator().next().getAssignedShardingItems().size(), Is.is(1));
        Assert.assertThat(actual.iterator().next().getAssignedShardingItems().get(0), Is.is(1));
        Mockito.verify(regCenter).isExisted("/state/failover");
        Mockito.verify(regCenter).remove("/state/failover/task_empty_job");
        Mockito.verify(regCenter).remove("/state/failover/not_existed_job");
    }
    
    @Test
    public void assertRemove() {
        String jobNodePath1 = TaskNode.builder().type(ExecutionType.FAILOVER).build().getTaskNodePath();
        String jobNodePath2 = TaskNode.builder().shardingItem(1).type(ExecutionType.FAILOVER).build().getTaskNodePath();
        failoverService.remove(Arrays.asList(TaskContext.MetaInfo.from(jobNodePath1), TaskContext.MetaInfo.from(jobNodePath2)));
        Mockito.verify(regCenter).remove("/state/failover/test_job/" + jobNodePath1);
        Mockito.verify(regCenter).remove("/state/failover/test_job/" + jobNodePath2);
    }
    
    @Test
    public void assertGetTaskId() {
        TaskNode taskNode = TaskNode.builder().type(ExecutionType.FAILOVER).build();
        failoverService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Mockito.when(regCenter.isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        Mockito.when(regCenter.get("/state/failover/test_job/" + taskNode.getTaskNodePath())).thenReturn(taskNode.getTaskNodeValue());
        Assert.assertThat(failoverService.getTaskId(taskNode.getMetaInfo()).get(), Is.is(taskNode.getTaskNodeValue()));
        Mockito.verify(regCenter, Mockito.times(2)).isExisted("/state/failover/test_job/" + taskNode.getTaskNodePath());
    }
    
    @Test
    public void assertGetAllFailoverTasksWithoutRootNode() {
        Mockito.when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(false);
        Assert.assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(FailoverNode.ROOT);
    }
    
    @Test
    public void assertGetAllFailoverTasksWhenRootNodeHasNoChild() {
        Mockito.when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Collections.emptyList());
        Assert.assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(FailoverNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
    }
    
    @Test
    public void assertGetAllFailoverTasksWhenJobNodeHasNoChild() {
        Mockito.when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Lists.newArrayList("test_job"));
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job"))).thenReturn(Collections.emptyList());
        Assert.assertTrue(failoverService.getAllFailoverTasks().isEmpty());
        Mockito.verify(regCenter).isExisted(FailoverNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job"));
    }
    
    @Test
    public void assertGetAllFailoverTasksWithRootNode() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String uuid3 = UUID.randomUUID().toString();
        Mockito.when(regCenter.isExisted(FailoverNode.ROOT)).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.ROOT)).thenReturn(Lists.newArrayList("test_job_1", "test_job_2"));
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_1"))).thenReturn(Lists.newArrayList("test_job_1@-@0", "test_job_1@-@1"));
        Mockito.when(regCenter.getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_2"))).thenReturn(Lists.newArrayList("test_job_2@-@0"));
        Mockito.when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@0"))).thenReturn(uuid1);
        Mockito.when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@1"))).thenReturn(uuid2);
        Mockito.when(regCenter.get(FailoverNode.getFailoverTaskNodePath("test_job_2@-@0"))).thenReturn(uuid3);
        Map<String, Collection<FailoverTaskInfo>> result = failoverService.getAllFailoverTasks();
        Assert.assertThat(result.size(), Is.is(2));
        Assert.assertThat(result.get("test_job_1").size(), Is.is(2));
        Assert.assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[0].getTaskInfo().toString(), Is.is("test_job_1@-@0"));
        Assert.assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[0].getOriginalTaskId(), Is.is(uuid1));
        Assert.assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[1].getTaskInfo().toString(), Is.is("test_job_1@-@1"));
        Assert.assertThat(result.get("test_job_1").toArray(new FailoverTaskInfo[]{})[1].getOriginalTaskId(), Is.is(uuid2));
        Assert.assertThat(result.get("test_job_2").size(), Is.is(1));
        Assert.assertThat(result.get("test_job_2").iterator().next().getTaskInfo().toString(), Is.is("test_job_2@-@0"));
        Assert.assertThat(result.get("test_job_2").iterator().next().getOriginalTaskId(), Is.is(uuid3));
        Mockito.verify(regCenter).isExisted(FailoverNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.ROOT);
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_1"));
        Mockito.verify(regCenter).getChildrenKeys(FailoverNode.getFailoverJobNodePath("test_job_2"));
        Mockito.verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@0"));
        Mockito.verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_1@-@1"));
        Mockito.verify(regCenter).get(FailoverNode.getFailoverTaskNodePath("test_job_2@-@0"));
    }
}
