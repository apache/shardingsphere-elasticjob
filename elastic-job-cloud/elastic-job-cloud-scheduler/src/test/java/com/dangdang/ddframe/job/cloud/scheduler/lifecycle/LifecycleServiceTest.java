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

package com.dangdang.ddframe.job.cloud.scheduler.lifecycle;

import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LifecycleServiceTest {
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @Mock
    private RunningService runningService;
    
    @InjectMocks
    private LifecycleService lifecycleService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(lifecycleService, "runningService", runningService);
    }
    
    @Test
    public void assertKillJob() {
        TaskContext taskContext = TaskContext.from(TaskNode.builder().shardingItem(0).build().getTaskNodeValue());
        when(runningService.getRunningTasks("test_job")).thenReturn(Collections.singletonList(taskContext));
        lifecycleService.killJob("test_job");
        verify(schedulerDriver).killTask(Protos.TaskID.newBuilder().setValue(taskContext.getId()).build());
    }
}
