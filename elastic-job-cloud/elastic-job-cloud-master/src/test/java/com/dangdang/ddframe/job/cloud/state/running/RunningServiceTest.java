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

package com.dangdang.ddframe.job.cloud.state.running;

import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.cloud.state.fixture.TaskNode;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
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
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job/" + nodePath)).thenReturn(true);
        runningService.add(TaskContext.fromMetaInfo(nodePath));
        verify(regCenter).isExisted("/state/running/test_job/" + nodePath);
        verify(regCenter, times(0)).persist("/state/running/test_job/" + nodePath, TaskNode.builder().build().getTaskNodeValue());
    }
    
    @Test
    public void assertAddWithoutRootNode() {
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        String nodeValue = TaskNode.builder().build().getTaskNodeValue();
        when(regCenter.isExisted("/state/running/test_job/" + nodePath)).thenReturn(false);
        runningService.add(TaskContext.fromId(nodeValue));
        verify(regCenter).isExisted("/state/running/test_job/" + nodePath);
        verify(regCenter).persist("/state/running/test_job/" + nodePath, nodeValue);
    }
    
    @Test
    public void assertRemoveWithoutRootNode() {
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        runningService.remove(TaskContext.fromMetaInfo(nodePath));
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
        runningService.remove(TaskContext.fromMetaInfo(nodePath1));
        verify(regCenter).remove("/state/running/test_job/" + nodePath1);
        verify(regCenter).remove("/state/running/test_job/" + nodePath2);
        verify(regCenter, times(0)).remove("/state/running/test_job/" + nodePath3);
        verify(regCenter, times(0)).remove("/state/running/test_job");
    }
    
    @Test
    public void assertRemoveWithRootNodeAndEmptyAfterRemove() {
        String nodePath = TaskNode.builder().build().getTaskNodePath();
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        runningService.remove(TaskContext.fromMetaInfo(nodePath));
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
        assertFalse(runningService.isTaskRunning(TaskContext.fromId(TaskNode.builder().build().getTaskNodeValue())));
    }
    
    @Test
    public void assertIsTaskRunningWitRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Collections.singletonList(TaskNode.builder().build().getTaskNodePath()));
        assertTrue(runningService.isTaskRunning(TaskContext.fromMetaInfo(TaskNode.builder().uuid("1").build().getTaskNodePath())));
        assertFalse(runningService.isTaskRunning(TaskContext.fromMetaInfo(TaskNode.builder().shardingItem(1).build().getTaskNodePath())));
    }
    @Test
    public void assertClear() {
        runningService.clear();
        verify(regCenter).remove("/state/running");
    }
}
