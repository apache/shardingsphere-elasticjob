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

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.fixture.LiteJsonConstants;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class ShardingListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ShardingService shardingService;
    
    private ShardingListenerManager shardingListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shardingListenerManager = new ShardingListenerManager(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(shardingListenerManager, shardingListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(shardingListenerManager, "shardingService", shardingService);
    }
    
    @Test
    public void assertStart() {
        shardingListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsNotConfigPath() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config/other", Type.NODE_ADDED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathButCurrentShardingTotalCountIsZero() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsEqualToNewShardingTotalCount() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 3);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(shardingService, times(0)).setReshardingFlag();
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 0);
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsNotEqualToNewShardingTotalCount() throws NoSuchFieldException {
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 5);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_UPDATED, LiteJsonConstants.getJobJson());
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 0);
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsNotServerStatusPath() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1/other", Type.NODE_ADDED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerStatusPathButUpdate() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1/status", Type.NODE_UPDATED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsInstanceChangeButJobInstanceIsShutdown() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/instances/xxx", Type.NODE_ADDED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsInstanceChange() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/instances/xxx", Type.NODE_ADDED, "");
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerChange() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_UPDATED, "");
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().shutdown("test_job");
    }
}
