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

package com.dangdang.ddframe.job.internal.execution;

import com.dangdang.ddframe.job.api.config.SimpleJobConfiguration;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.execution.ExecutionListenerManager.MonitorExecutionChangedJobListener;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class ExecutionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ExecutionService executionService;
    
    private final ExecutionListenerManager executionListenerManager = new ExecutionListenerManager(null, new SimpleJobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(executionListenerManager, executionListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(executionListenerManager, "executionService", executionService);
    }
    
    @Test
    public void assertStart() {
        executionListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<MonitorExecutionChangedJobListener>any());
    }
    
    @Test
    public void assertMonitorExecutionChangedJobListenerWhenIsNotMonitorExecutionPath() {
        executionListenerManager.new MonitorExecutionChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/other", null, "true".getBytes())), "/testJob/config/other");
        verify(executionService, times(0)).removeExecutionInfo();
    }
    
    @Test
    public void assertMonitorExecutionChangedJobListenerWhenIsMonitorExecutionPathButNotUpdate() {
        executionListenerManager.new MonitorExecutionChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/monitorExecution", null, "true".getBytes())), "/testJob/config/monitorExecution");
        verify(executionService, times(0)).removeExecutionInfo();
    }
    
    @Test
    public void assertMonitorExecutionChangedJobListenerWhenIsMonitorExecutionPathAndUpdateButEnable() {
        executionListenerManager.new MonitorExecutionChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/monitorExecution", null, "true".getBytes())), "/testJob/config/monitorExecution");
        verify(executionService, times(0)).removeExecutionInfo();
    }
    
    @Test
    public void assertMonitorExecutionChangedJobListenerWhenIsMonitorExecutionPathAndUpdateButDisable() {
        executionListenerManager.new MonitorExecutionChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/monitorExecution", null, "false".getBytes())), "/testJob/config/monitorExecution");
        verify(executionService).removeExecutionInfo();
    }
}
