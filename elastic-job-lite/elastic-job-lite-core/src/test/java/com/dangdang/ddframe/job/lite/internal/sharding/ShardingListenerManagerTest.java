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

package com.dangdang.ddframe.job.lite.internal.sharding;

import com.dangdang.ddframe.job.lite.fixture.LiteJsonConstants;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class ShardingListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionService executionService;
    
    private String ip = new LocalHostService().getIp();
    
    private final ShardingListenerManager shardingListenerManager = new ShardingListenerManager(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(shardingListenerManager, shardingListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(shardingListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(shardingListenerManager, "executionService", executionService);
    }
    
    @Test
    public void assertStart() {
        shardingListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(Matchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsNotConfigPath() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config/other", null, "".getBytes())), "/test_job/config/other");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathButCurrentShardingTotalCountIsZero() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config", null, LiteJsonConstants.getJobJson().getBytes())), "/test_job/config");
        verify(shardingService, times(0)).setReshardingFlag();
        verify(executionService, times(0)).setNeedFixExecutionInfoFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsEqualToNewShardingTotalCount() {
        shardingListenerManager.setCurrentShardingTotalCount(3);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config", null, LiteJsonConstants.getJobJson().getBytes())), "/test_job/config");
        verify(shardingService, times(0)).setReshardingFlag();
        verify(executionService, times(0)).setNeedFixExecutionInfoFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsNotEqualToNewShardingTotalCount() throws NoSuchFieldException {
        shardingListenerManager.setCurrentShardingTotalCount(5);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config", null, LiteJsonConstants.getJobJson().getBytes())), "/test_job/config");
        assertThat((Integer) ReflectionUtils.getFieldValue(shardingListenerManager, ShardingListenerManager.class.getDeclaredField("currentShardingTotalCount")), is(3));
        verify(shardingService).setReshardingFlag();
        verify(executionService).setNeedFixExecutionInfoFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsNotServerStatusPath() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + ip + "/other", null, "".getBytes())), "/test_job/servers/" + ip + "/other");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerStatusPathButUpdate() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/servers/" + ip + "/status", null, "".getBytes())), "/test_job/servers/" + ip + "/status");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerStatusPathAndAdd() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/servers/" + ip + "/status", null, "".getBytes())), "/test_job/servers/" + ip + "/status");
        verify(shardingService).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerStatusPathButUpdateAndIsServerDisabledPath() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/servers/" + ip + "/disabled", null, "".getBytes())), "/test_job/servers/" + ip + "/disabled");
        verify(shardingService).setReshardingFlag();
    }
}
