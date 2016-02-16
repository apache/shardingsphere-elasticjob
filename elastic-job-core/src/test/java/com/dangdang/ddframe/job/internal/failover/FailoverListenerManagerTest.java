/**
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

package com.dangdang.ddframe.job.internal.failover;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

public final class FailoverListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private FailoverService failoverService;
    
    private final FailoverListenerManager failoverListenerManager = new FailoverListenerManager(null, new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(failoverListenerManager, failoverListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverListenerManager, "configService", configService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "failoverService", failoverService);
    }
    
    @Test
    public void assertStart() {
        failoverListenerManager.start();
        verify(jobNodeStorage, times(3)).addDataListener(Matchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsNotRunningItemPath() {
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/execution/0/other", null, "".getBytes())), "/testJob/execution/0/other");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathButNotRemove() {
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/execution/0/running", null, "".getBytes())), "/testJob/execution/0/running");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveButItemCompleted() {
        when(executionService.isCompleted(0)).thenReturn(true);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/running", null, "".getBytes())), "/testJob/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedButDisableFaliover() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(false);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/running", null, "".getBytes())), "/testJob/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFalioverButHasRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(true);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/running", null, "".getBytes())), "/testJob/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFalioverAndHasNotRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(false);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/running", null, "".getBytes())), "/testJob/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsNotRunningItemPath() {
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/execution/0/other", null, "".getBytes())), "/testJob/execution/0/other");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathButNotRemove() {
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/execution/0/failover", null, "".getBytes())), "/testJob/execution/0/failover");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveButItemCompleted() {
        when(executionService.isCompleted(0)).thenReturn(true);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/failover", null, "".getBytes())), "/testJob/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedButDisableFaliover() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(false);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/failover", null, "".getBytes())), "/testJob/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFalioverButHasRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(true);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/failover", null, "".getBytes())), "/testJob/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFalioverAndHasNotRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.isFailover()).thenReturn(true);
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(false);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/execution/0/failover", null, "".getBytes())), "/testJob/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).isFailover();
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsNotFailoverPath() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/other", null, "".getBytes())), "/testJob/config/other");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/failover", null, "".getBytes())), "/testJob/config/failover");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/failover", null, "true".getBytes())), "/testJob/config/failover");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/failover", null, "false".getBytes())), "/testJob/config/failover");
        verify(failoverService).removeFailoverInfo();
    }
}
