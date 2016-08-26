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

package com.dangdang.ddframe.job.lite.internal.failover;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.LiteJsonConstants;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    
    private final FailoverListenerManager failoverListenerManager = new FailoverListenerManager(null, "test_job");
    
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
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/execution/0/other", null, "".getBytes())), "/test_job/execution/0/other");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathButNotRemove() {
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/execution/0/running", null, "".getBytes())), "/test_job/execution/0/running");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveButItemCompleted() {
        when(executionService.isCompleted(0)).thenReturn(true);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/running", null, "".getBytes())), "/test_job/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedButDisableFailover() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/running", null, "".getBytes())), "/test_job/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFailoverButHasRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(true);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/running", null, "".getBytes())), "/test_job/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFailoverAndHasNotRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(false);
        failoverListenerManager.new JobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/running", null, "".getBytes())), "/test_job/execution/0/running");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsNotRunningItemPath() {
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/execution/0/other", null, "".getBytes())), "/test_job/execution/0/other");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathButNotRemove() {
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/execution/0/failover", null, "".getBytes())), "/test_job/execution/0/failover");
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveButItemCompleted() {
        when(executionService.isCompleted(0)).thenReturn(true);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/failover", null, "".getBytes())), "/test_job/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedButDisableFailover() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build(), TestSimpleJob.class.getCanonicalName())).build());
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/failover", null, "".getBytes())), "/test_job/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService, times(0)).setCrashedFailoverFlag(0);
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFailoverButHasRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(true);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/failover", null, "".getBytes())), "/test_job/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverJobCrashedJobListenerWhenIsRunningItemPathAndRemoveAndItemNotCompletedAndEnableFailoverAndHasNotRunningItems() {
        when(executionService.isCompleted(0)).thenReturn(false);
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build(), 
                TestSimpleJob.class.getCanonicalName())).monitorExecution(true).build());
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(1, 2));
        when(executionService.hasRunningItems(Arrays.asList(1, 2))).thenReturn(false);
        failoverListenerManager.new FailoverJobCrashedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/execution/0/failover", null, "".getBytes())), "/test_job/execution/0/failover");
        verify(executionService).isCompleted(0);
        verify(configService).load(true);
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).hasRunningItems(Arrays.asList(1, 2));
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsNotFailoverPath() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config/other", null, LiteJsonConstants.getJobJson().getBytes())), "/test_job/config/other");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config/failover", null, "".getBytes())), "/test_job/config/failover");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/config", null, LiteJsonConstants.getJobJson().getBytes())), "/test_job/config");
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/config", null, LiteJsonConstants.getJobJson(false).getBytes())), "/test_job/config");
        verify(failoverService).removeFailoverInfo();
    }
}
