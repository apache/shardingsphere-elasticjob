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
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
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
    
    @Mock
    private SlaveCache slaveCache;
    
    private RunningService runningService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        runningService = new RunningService(regCenter);
        ReflectionUtils.setFieldValue(runningService, "slaveCache", slaveCache);
    }
    
    @Test
    public void assertLoad() {
        when(slaveCache.load("slave-S00")).thenReturn(Collections.singletonList(TaskContext.from("test_job@-@0@-@00")));
        assertThat(runningService.load("slave-S00"), is(Collections.singletonList(TaskContext.from("test_job@-@0@-@00"))));
        verify(slaveCache).load("slave-S00");
    }
    
    @Test
    public void assertAddWithRootNode() {
        when(regCenter.isExisted("/state/running/test_job/test_job@-@0@-@00")).thenReturn(true);
        runningService.add("slave-S00", TaskContext.from("test_job@-@0@-@00"));
        verify(regCenter).isExisted("/state/running/test_job/test_job@-@0@-@00");
        verify(regCenter, times(0)).persist("/state/running/test_job/test_job@-@0@-@00", "slave-S00");
        verify(slaveCache, times(0)).add("slave-S00", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertAddWithoutRootNode() {
        when(regCenter.isExisted("/state/running/test_job/test_job@-@0@-@00")).thenReturn(false);
        runningService.add("slave-S00", TaskContext.from("test_job@-@0@-@00"));
        verify(regCenter).isExisted("/state/running/test_job/test_job@-@0@-@00");
        verify(regCenter).persist("/state/running/test_job/test_job@-@0@-@00", "slave-S00");
        verify(slaveCache).add("slave-S00", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertRemoveWithoutRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        runningService.remove("slave-S00", TaskContext.from("test_job@-@0@-@00"));
        verify(regCenter, times(0)).remove("/state/running/test_job/test_job@-@0@-@00");
    }
    
    @Test
    public void assertRemoveWithRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Arrays.asList("test_job@-@0@-@00", "test_job@-@0@-@11", "test_job@-@1@-@00"));
        runningService.remove("slave-S00", TaskContext.from("test_job@-@0@-@00"));
        verify(regCenter).remove("/state/running/test_job/test_job@-@0@-@00");
        verify(regCenter).remove("/state/running/test_job/test_job@-@0@-@11");
        verify(regCenter, times(0)).remove("/state/running/test_job/test_job@-@1@-@00");
        verify(slaveCache).remove("slave-S00", TaskContext.from("test_job@-@0@-@00"));
    }
    
    @Test
    public void assertIsJobRunning() {
        when(regCenter.getChildrenKeys("/state/running/running_job")).thenReturn(Collections.singletonList("running_job@-@0@-@00"));
        assertTrue(runningService.isJobRunning("running_job"));
        assertFalse(runningService.isJobRunning("pending_job"));
        verify(regCenter).getChildrenKeys("/state/running/running_job");
        verify(regCenter).getChildrenKeys("/state/running/pending_job");
    }
    
    @Test
    public void assertIsTaskRunningWithoutRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(false);
        assertFalse(runningService.isTaskRunning(TaskContext.from("test_job@-@1@-@00")));
    }
    
    @Test
    public void assertIsTaskRunningWitRootNode() {
        when(regCenter.isExisted("/state/running/test_job")).thenReturn(true);
        when(regCenter.getChildrenKeys("/state/running/test_job")).thenReturn(Collections.singletonList("test_job@-@0@-@00"));
        assertTrue(runningService.isTaskRunning(TaskContext.from("test_job@-@0@-@11")));
        assertFalse(runningService.isTaskRunning(TaskContext.from("test_job@-@1@-@00")));
    }
    @Test
    public void assertClear() {
        runningService.clear();
        verify(regCenter).remove("/state/running");
    }
}