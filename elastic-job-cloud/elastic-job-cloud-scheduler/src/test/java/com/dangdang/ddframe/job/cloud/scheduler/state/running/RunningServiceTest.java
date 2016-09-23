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

package com.dangdang.ddframe.job.cloud.scheduler.state.running;

import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RunningServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @InjectMocks
    private RunningService runningService;
    
    @Test
    public void assertAddWithRootNode() {
        TaskNode taskNode = TaskNode.builder().build();
        when(regCenter.isExisted("/state/running/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        runningService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/running/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter, times(0)).persist("/state/running/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertAddWithoutRootNode() {
        TaskNode taskNode = TaskNode.builder().build();
        when(regCenter.isExisted("/state/running/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        runningService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        verify(regCenter).isExisted("/state/running/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter).persist("/state/running/test_job/" + taskNode.getTaskNodePath(), taskNode.getTaskNodeValue());
    }
    
    @Test
    public void assertUpdateDaemonStatusWithoutRunningNode() {
        TaskNode taskNode = TaskNode.builder().build();
        when(regCenter.isExisted("/state/running/test_job/" + taskNode.getTaskNodePath())).thenReturn(false);
        runningService.updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), true);
        verify(regCenter).isExisted("/state/running/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter, times(0)).persist("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle", "");
        verify(regCenter, times(0)).remove("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle");
    }
    
    @Test
    public void assertUpdateDaemonStatusWithRunningNodeAndIdle() {
        TaskNode taskNode = TaskNode.builder().build();
        when(regCenter.isExisted("/state/running/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        runningService.updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), true);
        verify(regCenter).isExisted("/state/running/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter).persist("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle", "");
        verify(regCenter, times(0)).remove("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle");
    }
    
    @Test
    public void assertUpdateDaemonStatusWithRunningNodeAndNotIdle() {
        TaskNode taskNode = TaskNode.builder().build();
        when(regCenter.isExisted("/state/running/test_job/" + taskNode.getTaskNodePath())).thenReturn(true);
        runningService.updateDaemonStatus(TaskContext.from(taskNode.getTaskNodeValue()), false);
        verify(regCenter).isExisted("/state/running/test_job/" + taskNode.getTaskNodePath());
        verify(regCenter, times(0)).persist("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle", "");
        verify(regCenter).remove("/state/running/test_job/" + taskNode.getTaskNodePath() + "/idle");
    }
    
    @Test
    public void assertRemoveWithoutRootNode() {
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        runningService.remove(TaskContext.MetaInfo.from(nodePath));
        verify(regCenter).remove("/state/running/test_job/" + nodePath);
        verify(regCenter, times(0)).getChildrenKeys("/state/running/test_job");
        verify(regCenter, times(0)).remove("/state/running/test_job");
    }
    
    @Test
    public void assertRemoveWithRootNodeAndNotEmpty() {
        String nodePath1 = TaskNode.builder().build().getTaskNodePath();
        String nodePath2 = TaskNode.builder().uuid("1").build().getTaskNodePath();
        String nodePath3 = TaskNode.builder().shardingItem(1).build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Arrays.asList(nodePath1, nodePath2, nodePath3));
        runningService.remove(TaskContext.MetaInfo.from(nodePath1));
        verify(regCenter).remove("/state/running/test_job/" + nodePath1);
        verify(regCenter).remove("/state/running/test_job/" + nodePath2);
        verify(regCenter, times(0)).remove("/state/running/test_job/" + nodePath3);
        verify(regCenter, times(0)).remove("/state/running/test_job");
    }
    
    @Test
    public void assertRemoveWithRootNodeAndEmptyAfterRemove() {
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        runningService.remove(TaskContext.MetaInfo.from(nodePath));
        verify(regCenter).remove("/state/running/test_job/" + nodePath);
        verify(regCenter).remove("/state/running/test_job");
    }
    
    @Test
    public void assertIsJobRunning() {
        when(regCenter.getChildrenKeys("/state/running/running_job")).thenReturn(Collections.singletonList(TaskNode.builder().build().getTaskNodeValue()));
        assertTrue(runningService.isJobRunning("running_job"));
        assertFalse(runningService.isJobRunning("pending_job"));
        verify(regCenter).getChildrenKeys("/state/running/running_job");
        verify(regCenter).getChildrenKeys("/state/running/pending_job");
    }
    
    @Test
    public void assertIsTaskRunningWithoutRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        assertFalse(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
    }
    
    @Test
    public void assertIsTaskRunningWitRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Collections.singletonList(TaskNode.builder().build().getTaskNodePath()));
        assertTrue(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().uuid("1").build().getTaskNodePath())));
        assertFalse(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().shardingItem(1).build().getTaskNodePath())));
    }
    
    @Test
    public void assertGetRunningTasksWithoutRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        assertThat(runningService.getRunningTasks("test_job"), Is.<Collection<TaskContext>>is(Collections.<TaskContext>emptyList()));
    }
    
    @Test
    public void assertGetRunningTasks() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Arrays.asList("test_job@-@0", "test_job@-@1"));
        when(regCenter.get("/state/running/test_job/test_job@-@0")).thenReturn("test_job@-@0@-@READY@-@SLAVE-S0");
        when(regCenter.get("/state/running/test_job/test_job@-@1")).thenReturn("test_job@-@1@-@READY@-@SLAVE-S0");
        assertThat(runningService.getRunningTasks("test_job"), 
                Is.<Collection<TaskContext>>is(Arrays.asList(TaskContext.from("test_job@-@0@-@READY@-@SLAVE-S0"), TaskContext.from("test_job@-@1@-@READY@-@SLAVE-S0"))));
    }
    
    @Test
    public void assertClear() {
        runningService.clear();
        verify(regCenter).remove("/state/running");
    }
}
