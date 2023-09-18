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

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class ShardingServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private InstanceService instanceService;
    
    private final ShardingService shardingService = new ShardingService(null, "test_job");
    
    @BeforeEach
    public void setUp() {
        ReflectionUtils.setFieldValue(shardingService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(shardingService, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(shardingService, "configService", configService);
        ReflectionUtils.setFieldValue(shardingService, "executionService", executionService);
        ReflectionUtils.setFieldValue(shardingService, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(shardingService, "serverService", serverService);
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
    }
    
    @Test
    public void assertSetReshardingFlagOnLeader() {
        when(leaderService.isLeaderUntilBlock()).thenReturn(true);
        shardingService.setReshardingFlag();
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/sharding/necessary");
    }
    
    @Test
    public void assertSetReshardingFlagOnNonLeader() {
        when(leaderService.isLeaderUntilBlock()).thenReturn(false);
        shardingService.setReshardingFlag();
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("leader/sharding/necessary");
    }
    
    @Test
    public void assertIsNeedSharding() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        assertTrue(shardingService.isNeedSharding());
    }
    
    @Test
    public void assertShardingWhenUnnecessary() {
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode(ShardingNode.PROCESSING, "");
    }
    
    @Test
    public void assertShardingWithoutAvailableJobInstances() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode(ShardingNode.PROCESSING, "");
    }
    
    @Test
    public void assertShardingWhenIsNotLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true, false);
        when(instanceService.getAvailableJobInstances()).thenReturn(Collections.singletonList(new JobInstance("127.0.0.1@-@0")));
        when(leaderService.isLeaderUntilBlock()).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/processing")).thenReturn(true, false);
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode(ShardingNode.PROCESSING, "");
    }
    
    @Test
    public void assertShardingNecessaryWhenMonitorExecutionEnabledAndIncreaseShardingTotalCount() {
        when(instanceService.getAvailableJobInstances()).thenReturn(Collections.singletonList(new JobInstance("127.0.0.1@-@0")));
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        when(leaderService.isLeaderUntilBlock()).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(true).build());
        when(executionService.hasRunningItems()).thenReturn(true, false);
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(Arrays.asList("0", "1"));
        shardingService.shardingIfNecessary();
        verify(executionService, times(2)).hasRunningItems();
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/0");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/1");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/2");
        verify(jobNodeStorage).fillEphemeralJobNode("leader/sharding/processing", "");
        verify(jobNodeStorage).executeInTransaction(any(List.class));
    }
    
    @Test
    public void assertShardingNecessaryWhenMonitorExecutionDisabledAndDecreaseShardingTotalCount() {
        when(instanceService.getAvailableJobInstances()).thenReturn(Collections.singletonList(new JobInstance("127.0.0.1@-@0")));
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        when(leaderService.isLeaderUntilBlock()).thenReturn(true);
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").monitorExecution(false).build());
        when(jobNodeStorage.getJobNodeChildrenKeys(ShardingNode.ROOT)).thenReturn(Arrays.asList("0", "1", "2", "3"));
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/0/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/0");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/1/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/1");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/2/instance");
        verify(jobNodeStorage).createJobNodeIfNeeded("sharding/2");
        verify(jobNodeStorage, times(0)).removeJobNodeIfExisted("execution/2");
        verify(jobNodeStorage).removeJobNodeIfExisted("sharding/3");
        verify(jobNodeStorage).fillEphemeralJobNode("leader/sharding/processing", "");
        verify(jobNodeStorage).executeInTransaction(any(List.class));
    }
    
    @Test
    public void assertGetShardingItemsWithNotAvailableServer() {
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nserverIp: 127.0.0.1\n");
        assertThat(shardingService.getShardingItems("127.0.0.1@-@0"), is(Collections.<Integer>emptyList()));
    }
    
    @Test
    public void assertGetShardingItemsWithAvailableServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeData("sharding/0/instance")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeData("sharding/1/instance")).thenReturn("127.0.0.1@-@1");
        when(jobNodeStorage.getJobNodeData("sharding/2/instance")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nserverIp: 127.0.0.1\n");
        assertThat(shardingService.getShardingItems("127.0.0.1@-@0"), is(Arrays.asList(0, 2)));
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertGetLocalShardingItemsWithInstanceShutdown() {
        assertThat(shardingService.getLocalShardingItems(), is(Collections.<Integer>emptyList()));
    }
    
    @Test
    public void assertGetLocalShardingItemsWithDisabledServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        assertThat(shardingService.getLocalShardingItems(), is(Collections.<Integer>emptyList()));
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertGetLocalShardingItemsWithEnabledServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeData("sharding/0/instance")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeData("sharding/1/instance")).thenReturn("127.0.0.1@-@1");
        when(jobNodeStorage.getJobNodeData("sharding/2/instance")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nserverIp: 127.0.0.1\n");
        assertThat(shardingService.getLocalShardingItems(), is(Arrays.asList(0, 2)));
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertHasShardingInfoInOfflineServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("host0@-@0", "host0@-@1"));
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(0))).thenReturn("host0@-@0");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(1))).thenReturn("host0@-@1");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(2))).thenReturn("host0@-@2");
        assertTrue(shardingService.hasShardingInfoInOfflineServers());
    }
    
    @Test
    public void assertHasNotShardingInfoInOfflineServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("host0@-@0", "host0@-@1"));
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(0))).thenReturn("host0@-@0");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(1))).thenReturn("host0@-@1");
        when(jobNodeStorage.getJobNodeData(ShardingNode.getInstanceNode(2))).thenReturn("host0@-@0");
        assertFalse(shardingService.hasShardingInfoInOfflineServers());
    }
    
    @Test
    public void assertGetCrashedShardingItemsWithNotEnableServer() {
        assertThat(shardingService.getCrashedShardingItems("127.0.0.1@-@0"), is(Collections.<Integer>emptyList()));
    }

    @Test
    public void assertGetCrashedShardingItemsWithEnabledServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isEnableServer("127.0.0.1")).thenReturn(true);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        when(jobNodeStorage.getJobNodeData("sharding/0/instance")).thenReturn("127.0.0.1@-@0");
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("sharding/2/instance")).thenReturn("127.0.0.1@-@0");
        lenient().when(jobNodeStorage.isJobNodeExisted("sharding/2/running")).thenReturn(true);
        assertThat(shardingService.getCrashedShardingItems("127.0.0.1@-@0"), is(Arrays.asList(0, 2)));
        JobRegistry.getInstance().shutdown("test_job");
    }
}
