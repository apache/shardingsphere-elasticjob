/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import org.apache.curator.framework.recipes.cache.CuratorCacheListener.Type;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
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
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        shardingListenerManager = new ShardingListenerManager(null, "test_job");
        ReflectionUtils.setSuperclassFieldValue(shardingListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(shardingListenerManager, "shardingService", shardingService);
    }
    
    @Test
    public void assertStart() {
        shardingListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsNotConfigPath() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config/other", Type.NODE_CREATED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathButCurrentShardingTotalCountIsZero() {
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_CREATED, LiteYamlConstants.getJobYaml());
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsEqualToNewShardingTotalCount() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 3);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_CREATED, LiteYamlConstants.getJobYaml());
        verify(shardingService, times(0)).setReshardingFlag();
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 0);
    }
    
    @Test
    public void assertShardingTotalCountChangedJobListenerWhenIsConfigPathAndCurrentShardingTotalCountIsNotEqualToNewShardingTotalCount() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 5);
        shardingListenerManager.new ShardingTotalCountChangedJobListener().dataChanged("/test_job/config", Type.NODE_CHANGED, LiteYamlConstants.getJobYaml());
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().setCurrentShardingTotalCount("test_job", 0);
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsNotServerStatusPath() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1/other", Type.NODE_CREATED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerStatusPathButUpdate() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1/status", Type.NODE_CHANGED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsInstanceChangeButJobInstanceIsShutdown() {
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/instances/xxx", Type.NODE_CREATED, "");
        verify(shardingService, times(0)).setReshardingFlag();
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsInstanceChange() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/instances/xxx", Type.NODE_CREATED, "");
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertListenServersChangedJobListenerWhenIsServerChange() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        shardingListenerManager.new ListenServersChangedJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_CHANGED, "");
        verify(shardingService).setReshardingFlag();
        JobRegistry.getInstance().shutdown("test_job");
    }
}
