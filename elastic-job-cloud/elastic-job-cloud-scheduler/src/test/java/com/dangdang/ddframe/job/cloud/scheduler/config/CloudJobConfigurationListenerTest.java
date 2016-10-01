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

package com.dangdang.ddframe.job.cloud.scheduler.config;

import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJsonConstants;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.google.common.collect.Lists;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobConfigurationListenerTest {
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private SchedulerDriver schedulerDriver;
    
    @InjectMocks
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "readyService", readyService);
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "runningService", runningService);
    }
    
    @Test
    public void assertChildEventWhenDataIsNull() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, null));
        verify(readyService, times(0)).addDaemon(Matchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsNotUpdate() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/test_job", null, "".getBytes())));
        verify(readyService, times(0)).addDaemon(Matchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsNotConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/other/test_job", null, "".getBytes())));
        verify(readyService, times(0)).addDaemon(Matchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsRemovedAndIsRootConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/test_job", null, "".getBytes())));
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(Type.NODE_REMOVED, new ChildData("/config", null, "".getBytes())));
        verify(readyService, times(0)).remove(Lists.<String>newArrayList());
    }
    
    @Test
    public void assertChildEventWhenStateIsRemovedAndIsJobConfigPath() throws Exception {
        when(runningService.getRunningTasks("test_job")).thenReturn(Arrays.asList(
                TaskContext.from("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID")));
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/test_job", null, "".getBytes())));
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(Type.NODE_REMOVED, new ChildData("/config/test_job", null, "".getBytes())));
        verify(schedulerDriver).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(schedulerDriver).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(runningService).remove(TaskContext.MetaInfo.from("test_job@-@0"));
        verify(runningService).remove(TaskContext.MetaInfo.from("test_job@-@1"));
        verify(readyService).remove(Lists.newArrayList("test_job"));
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndIsTransientJob() throws Exception {
        when(runningService.getRunningTasks("test_job")).thenReturn(Arrays.asList(
                TaskContext.from("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID")));
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/config/test_job", null, CloudJsonConstants.getJobJson().getBytes())));
        verify(schedulerDriver, times(0)).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(schedulerDriver, times(0)).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(runningService, times(0)).remove(TaskContext.MetaInfo.from("test_job@-@0"));
        verify(runningService, times(0)).remove(TaskContext.MetaInfo.from("test_job@-@1"));
        verify(readyService, times(0)).addDaemon(Matchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathIsDaemonJob() throws Exception {
        when(runningService.getRunningTasks("test_job")).thenReturn(Arrays.asList(
                TaskContext.from("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID")));
        cloudJobConfigurationListener.childEvent(
                null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/config/test_job", null, CloudJsonConstants.getJobJson(JobExecutionType.DAEMON).getBytes())));
        verify(schedulerDriver).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@0@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(schedulerDriver).killTask(Protos.TaskID.getDefaultInstance().toBuilder().setValue("test_job@-@1@-@READY@-@SLAVE-S0@-@UUID").build());
        verify(runningService).remove(TaskContext.MetaInfo.from("test_job@-@0"));
        verify(runningService).remove(TaskContext.MetaInfo.from("test_job@-@1"));
        verify(readyService).addDaemon(Matchers.<String>any());
    }
}
