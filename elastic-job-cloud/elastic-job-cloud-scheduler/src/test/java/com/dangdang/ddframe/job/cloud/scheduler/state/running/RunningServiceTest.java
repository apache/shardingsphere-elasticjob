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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class RunningServiceTest {
    
    private RunningService runningService;
    
    private TaskContext taskContext;
    
    @Before
    public void setUp() {
        runningService = new RunningService();
        taskContext = TaskContext.from(TaskNode.builder().build().getTaskNodeValue());
        runningService.add(taskContext);
    }
    
    @After
    public void tearDown() {
        runningService.clear();
    }
    
    @Test
    public void assertAddWithoutData() {
        assertThat(runningService.getRunningTasks("test_job").size(), is(1));
        assertThat(runningService.getRunningTasks("test_job").iterator().next(), is(taskContext));
    }
    
    @Test
    public void assertAddWithData() {
        TaskNode taskNode = TaskNode.builder().jobName("other_job").build();
        runningService.add(TaskContext.from(taskNode.getTaskNodeValue()));
        assertThat(runningService.getRunningTasks("other_job").size(), is(1));
        assertThat(runningService.getRunningTasks("other_job").iterator().next(), is(TaskContext.from(taskNode.getTaskNodeValue())));
    }
    
    @Test
    public void assertUpdateIdle() {
        runningService.updateIdle(taskContext, true);
        assertThat(runningService.getRunningTasks("test_job").size(), is(1));
        assertTrue(runningService.getRunningTasks("test_job").iterator().next().isIdle());
    }
    
    @Test
    public void assertRemove() {
        runningService.remove(taskContext);
        assertTrue(runningService.getRunningTasks("test_job").isEmpty());
    }
    
    @Test
    public void assertIsJobRunning() {
        assertTrue(runningService.isJobRunning("test_job"));
    }
    
    @Test
    public void assertIsTaskRunning() {
        assertTrue(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().build().getTaskNodePath())));
    }
    
    @Test
    public void assertIsTaskNotRunning() {
        assertFalse(runningService.isTaskRunning(TaskContext.MetaInfo.from(TaskNode.builder().shardingItem(2).build().getTaskNodePath())));
    }
    
    @Test
    public void assertMappingOperate() {
        String taskId = TaskNode.builder().build().getTaskNodeValue();
        assertNull(runningService.popMapping(taskId));
        runningService.addMapping(taskId, "localhost");
        assertThat(runningService.popMapping(taskId), is("localhost"));
        assertNull(runningService.popMapping(taskId));
    }
    
    @Test
    public void assertClear() {
        assertFalse(runningService.getRunningTasks("test_job").isEmpty());
        runningService.addMapping(TaskNode.builder().build().getTaskNodeValue(), "localhost");
        runningService.clear();
        assertTrue(runningService.getRunningTasks("test_job").isEmpty());
        assertNull(runningService.popMapping(TaskNode.builder().build().getTaskNodeValue()));
    }
}
