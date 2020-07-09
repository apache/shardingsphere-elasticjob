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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running;

import org.apache.shardingsphere.elasticjob.cloud.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public final class RunningServiceTest {
    
    private TaskContext taskContext;
    
    private TaskContext taskContextT;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private RunningService runningService;
    
    @Before
    public void setUp() {
        Mockito.when(regCenter.get("/config/job/test_job")).thenReturn(CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON));
        Mockito.when(regCenter.get("/config/job/test_job_t")).thenReturn(CloudJsonConstants.getJobJson("test_job_t"));
        runningService = new RunningService(regCenter);
        taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        taskContextT = TaskContext.from(TaskNode.builder().jobName("test_job_t").build().getTaskNodeValue());
        runningService.add(taskContext);
        runningService.add(taskContextT);
        Assert.assertThat(runningService.getAllRunningDaemonTasks().size(), Is.is(1));
        Assert.assertThat(runningService.getAllRunningTasks().size(), Is.is(2));
        String path = RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString());
        Mockito.verify(regCenter).isExisted(path);
        Mockito.verify(regCenter).persist(path, taskContext.getId());
    }
    
    @After
    public void tearDown() {
        runningService.clear();
    }
    
    @Test
    public void assertStart() {
        TaskNode taskNode1 = TaskNode.builder().jobName("test_job").shardingItem(0).slaveId("111").type(ExecutionType.READY).uuid(UUID.randomUUID().toString()).build();
        TaskNode taskNode2 = TaskNode.builder().jobName("test_job").shardingItem(1).slaveId("222").type(ExecutionType.FAILOVER).uuid(UUID.randomUUID().toString()).build();
        Mockito.when(regCenter.getChildrenKeys(RunningNode.ROOT)).thenReturn(Collections.singletonList("test_job"));
        Mockito.when(regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath("test_job"))).thenReturn(Arrays.asList(taskNode1.getTaskNodePath(), taskNode2.getTaskNodePath()));
        Mockito.when(regCenter.get(RunningNode.getRunningTaskNodePath(taskNode1.getTaskNodePath()))).thenReturn(taskNode1.getTaskNodeValue());
        Mockito.when(regCenter.get(RunningNode.getRunningTaskNodePath(taskNode2.getTaskNodePath()))).thenReturn(taskNode2.getTaskNodeValue());
        runningService.start();
        Assert.assertThat(runningService.getAllRunningDaemonTasks().size(), Is.is(2));
    }
    
    @Test
    public void assertAddWithoutData() {
        Assert.assertThat(runningService.getRunningTasks("test_job").size(), Is.is(1));
        Assert.assertThat(runningService.getRunningTasks("test_job").iterator().next(), Is.is(taskContext));
        Assert.assertThat(runningService.getRunningTasks("test_job_t").size(), Is.is(1));
        Assert.assertThat(runningService.getRunningTasks("test_job_t").iterator().next(), Is.is(taskContextT));
    }
    
    @Test
    public void assertAddWithData() {
        Mockito.when(regCenter.get("/config/job/other_job")).thenReturn(CloudJsonConstants.getJobJson("other_job"));
        TaskNode taskNode = TaskNode.builder().jobName("other_job").build();
        runningService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        Assert.assertThat(runningService.getRunningTasks("other_job").size(), Is.is(1));
        Assert.assertThat(runningService.getRunningTasks("other_job").iterator().next(), Is.is(TaskContext.from(taskNode.getTaskNodeValue())));
    }
    
    @Test
    public void assertUpdateIdle() {
        runningService.updateIdle(taskContext, true);
        Assert.assertThat(runningService.getRunningTasks("test_job").size(), Is.is(1));
        Assert.assertTrue(runningService.getRunningTasks("test_job").iterator().next().isIdle());
    }
    
    @Test
    public void assertRemoveByJobName() {
        runningService.remove("test_job");
        Assert.assertTrue(runningService.getRunningTasks("test_job").isEmpty());
        Mockito.verify(regCenter).remove(RunningNode.getRunningJobNodePath("test_job"));
        runningService.remove("test_job_t");
        Assert.assertTrue(runningService.getRunningTasks("test_job_t").isEmpty());
    }
    
    @Test
    public void assertRemoveByTaskContext() {
        Mockito.when(regCenter.isExisted(RunningNode.getRunningJobNodePath("test_job"))).thenReturn(true);
        Mockito.when(regCenter.getChildrenKeys(RunningNode.getRunningJobNodePath("test_job"))).thenReturn(Collections.<String>emptyList());
        runningService.remove(taskContext);
        Assert.assertTrue(runningService.getRunningTasks("test_job").isEmpty());
        Mockito.verify(regCenter).remove(RunningNode.getRunningTaskNodePath(taskContext.getMetaInfo().toString()));
        runningService.remove(taskContextT);
        Assert.assertTrue(runningService.getRunningTasks("test_job_t").isEmpty());
    }
    
    @Test
    public void assertIsJobRunning() {
        Assert.assertTrue(runningService.isJobRunning("test_job"));
    }
    
    @Test
    public void assertIsTaskRunning() {
        Assert.assertTrue(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
    }
    
    @Test
    public void assertIsTaskNotRunning() {
        Assert.assertFalse(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().shardingItem(2).build().getTaskNodePath())));
    }
    
    @Test
    public void assertMappingOperate() {
        String taskId = TaskNode.builder().build().getTaskNodeValue();
        Assert.assertNull(runningService.popMapping(taskId));
        runningService.addMapping(taskId, "localhost");
        Assert.assertThat(runningService.popMapping(taskId), Is.is("localhost"));
        Assert.assertNull(runningService.popMapping(taskId));
    }
    
    @Test
    public void assertClear() {
        Assert.assertFalse(runningService.getRunningTasks("test_job").isEmpty());
        runningService.addMapping(TaskNode.builder().build().getTaskNodeValue(), "localhost");
        runningService.clear();
        Assert.assertTrue(runningService.getRunningTasks("test_job").isEmpty());
        Assert.assertNull(runningService.popMapping(TaskNode.builder().build().getTaskNodeValue()));
    }
}
