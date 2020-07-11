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

package org.apache.shardingsphere.elasticjob.lite.internal.failover;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FailoverServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ShardingService shardingService;
    
    private final FailoverService failoverService = new FailoverService(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(failoverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverService, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverService, "jobName", "test_job");
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
    }
    
    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsNotAssigned() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("leader/failover/items/0");
    }
    
    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsAssigned() {
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(false);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/failover/items/0");
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeIsEmpty() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Collections.emptyList());
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenServerIsNotReady() {
        JobRegistry.getInstance().setJobRunning("test_job", true);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage).executeInLeader(eq("leader/failover/latch"), ArgumentMatchers.<FailoverService.FailoverLeaderExecutionCallback>any());
        JobRegistry.getInstance().setJobRunning("test_job", false);
    }
    
    @Test
    public void assertFailoverLeaderExecutionCallbackIfNotNecessary() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).getJobNodeChildrenKeys("leader/failover/items");
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertFailoverLeaderExecutionCallbackIfNecessary() {
        JobRegistry.getInstance().setJobRunning("test_job", false);
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(2)).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage).fillEphemeralJobNode("sharding/0/failover", "127.0.0.1@-@0");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/failover/items/0");
        verify(jobScheduleController).triggerJob();
        JobRegistry.getInstance().setJobRunning("test_job", false);
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertUpdateFailoverComplete() {
        failoverService.updateFailoverComplete(Arrays.asList(0, 1));
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/failover");
    }
    
    @Test
    public void assertGetFailoverItems() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/0/failover")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/1/failover")).thenReturn("127.0.0.1@-@1");
        assertThat(failoverService.getFailoverItems("127.0.0.1@-@1"), is(Collections.singletonList(1)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/0/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/1/failover");
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertGetLocalFailoverItemsIfShutdown() {
        assertThat(failoverService.getLocalFailoverItems(), is(Collections.<Integer>emptyList()));
        verify(jobNodeStorage, times(0)).getJobNodeChildrenKeys("sharding");
    }
    
    @Test
    public void assertGetLocalFailoverItems() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/0/failover")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeDataDirectly("sharding/1/failover")).thenReturn("127.0.0.1@-@1");
        assertThat(failoverService.getLocalFailoverItems(), is(Collections.singletonList(0)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/0/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("sharding/1/failover");
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertGetLocalTakeOffItems() {
        when(shardingService.getLocalShardingItems()).thenReturn(Arrays.asList(0, 1, 2));
        when(jobNodeStorage.isJobNodeExisted("sharding/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("sharding/2/failover")).thenReturn(false);
        assertThat(failoverService.getLocalTakeOffItems(), is(Arrays.asList(0, 1)));
        verify(shardingService).getLocalShardingItems();
        verify(jobNodeStorage).isJobNodeExisted("sharding/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/1/failover");
        verify(jobNodeStorage).isJobNodeExisted("sharding/2/failover");
    }
    
    @Test
    public void assertRemoveFailoverInfo() {
        when(jobNodeStorage.getJobNodeChildrenKeys("sharding")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.removeFailoverInfo();
        verify(jobNodeStorage).getJobNodeChildrenKeys("sharding");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/failover");
    }
}
