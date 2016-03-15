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

package com.dangdang.ddframe.job.internal.sharding;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.internal.storage.TransactionExecutionCallback;
import com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;
import org.apache.curator.framework.api.transaction.CuratorTransactionBridge;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.TransactionCheckBuilder;
import org.apache.curator.framework.api.transaction.TransactionCreateBuilder;
import org.apache.curator.framework.api.transaction.TransactionDeleteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private ServerService serverService;
    
    private final JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private final ShardingService shardingService = new ShardingService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(shardingService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(shardingService, "localHostService", localHostService);
        ReflectionUtils.setFieldValue(shardingService, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(shardingService, "configService", configService);
        ReflectionUtils.setFieldValue(shardingService, "executionService", executionService);
        ReflectionUtils.setFieldValue(shardingService, "serverService", serverService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
    }
    
    @Test
    public void assertSetReshardingFlag() {
        shardingService.setReshardingFlag();
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/sharding/necessary");
    }
    
    @Test
    public void assertIsNeedSharding() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        assertTrue(shardingService.isNeedSharding());
        verify(jobNodeStorage).isJobNodeExisted("leader/sharding/necessary");
    }
    
    @Test
    public void assertShardingWhenUnnecessary() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(false);
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/sharding/necessary");
    }
    
    @Test
    public void assertShardingWhenIsNotLeaderAndIsShardingProcessing() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true, true, false, false);
        when(leaderElectionService.isLeader()).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/processing")).thenReturn(true, false);
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage, times(4)).isJobNodeExisted("leader/sharding/necessary");
        verify(jobNodeStorage, times(2)).isJobNodeExisted("leader/sharding/processing");
    }
    
    @Test
    public void assertShardingNecessaryWhenMonitorExecutionEnabled() {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(configService.isMonitorExecution()).thenReturn(true);
        when(executionService.hasRunningItems()).thenReturn(true, false);
        when(configService.getJobShardingStrategyClass()).thenReturn(AverageAllocationJobShardingStrategy.class.getCanonicalName());
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.getShardingItemParameters()).thenReturn(Collections.<Integer, String>emptyMap());
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/sharding/necessary");
        verify(leaderElectionService).isLeader();
        verify(configService).isMonitorExecution();
        verify(executionService, times(2)).hasRunningItems();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/sharding/processing", "");
        verify(configService).getJobShardingStrategyClass();
        verify(configService).getShardingTotalCount();
        verify(configService).getShardingItemParameters();
        verify(jobNodeStorage, times(2)).executeInTransaction(any(TransactionExecutionCallback.class));
    }
    
    @Test
    public void assertShardingNecessaryWhenMonitorExecutionDisabled() throws Exception {
        when(jobNodeStorage.isJobNodeExisted("leader/sharding/necessary")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(configService.isMonitorExecution()).thenReturn(false);
        when(configService.getJobShardingStrategyClass()).thenReturn(AverageAllocationJobShardingStrategy.class.getCanonicalName());
        when(configService.getShardingTotalCount()).thenReturn(3);
        when(configService.getShardingItemParameters()).thenReturn(Collections.<Integer, String>emptyMap());
        shardingService.shardingIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/sharding/necessary");
        verify(leaderElectionService).isLeader();
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/sharding/processing", "");
        verify(configService).getJobShardingStrategyClass();
        verify(configService).getShardingTotalCount();
        verify(configService).getShardingItemParameters();
        verify(jobNodeStorage, times(2)).executeInTransaction(any(TransactionExecutionCallback.class));
    }
    
    @Test
    public void assertGetLocalHostShardingItemsWhenNodeExisted() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/sharding")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/sharding")).thenReturn("0,1,2");
        assertThat(shardingService.getLocalHostShardingItems(), is(Arrays.asList(0, 1, 2)));
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/sharding");
        verify(jobNodeStorage).getJobNodeDataDirectly("servers/mockedIP/sharding");
    }
    
    @Test
    public void assertGetLocalHostShardingWhenNodeNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/sharding")).thenReturn(false);
        assertThat(shardingService.getLocalHostShardingItems(), is(Collections.EMPTY_LIST));
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/sharding");
    }
    
    @Test
    public void assertClearShardingInfoInfoTransactionExecutionCallback() throws Exception {
        when(serverService.getAllServers()).thenReturn(Collections.singletonList("host0"));
        CuratorTransactionFinal curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        TransactionCheckBuilder transactionCheckBuilder = mock(TransactionCheckBuilder.class);
        TransactionDeleteBuilder transactionDeleteBuilder = mock(TransactionDeleteBuilder.class);
        CuratorTransactionBridge curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        when(curatorTransactionFinal.check()).thenReturn(transactionCheckBuilder);
        when(transactionCheckBuilder.forPath("/testJob/servers/host0/sharding")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        when(curatorTransactionFinal.delete()).thenReturn(transactionDeleteBuilder);
        when(transactionDeleteBuilder.forPath("/testJob/servers/host0/sharding")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        ShardingService.ClearShardingInfoInfoTransactionExecutionCallback actual = shardingService.new ClearShardingInfoInfoTransactionExecutionCallback();
        actual.execute(curatorTransactionFinal);
        verify(serverService).getAllServers();
        verify(curatorTransactionFinal).check();
        verify(transactionCheckBuilder).forPath("/testJob/servers/host0/sharding");
        verify(curatorTransactionFinal).delete();
        verify(transactionDeleteBuilder).forPath("/testJob/servers/host0/sharding");
        verify(curatorTransactionBridge, times(2)).and();
    }
    
    @Test
    public void assertPersistShardingInfoTransactionExecutionCallback() throws Exception {
        CuratorTransactionFinal curatorTransactionFinal = mock(CuratorTransactionFinal.class);
        TransactionCreateBuilder transactionCreateBuilder = mock(TransactionCreateBuilder.class);
        TransactionDeleteBuilder transactionDeleteBuilder = mock(TransactionDeleteBuilder.class);
        CuratorTransactionBridge curatorTransactionBridge = mock(CuratorTransactionBridge.class);
        when(curatorTransactionFinal.create()).thenReturn(transactionCreateBuilder);
        when(transactionCreateBuilder.forPath("/testJob/servers/host0/sharding", "0,1,2".getBytes())).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        when(curatorTransactionFinal.delete()).thenReturn(transactionDeleteBuilder);
        when(transactionDeleteBuilder.forPath("/testJob/leader/sharding/necessary")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        when(curatorTransactionFinal.delete()).thenReturn(transactionDeleteBuilder);
        when(transactionDeleteBuilder.forPath("/testJob/leader/sharding/processing")).thenReturn(curatorTransactionBridge);
        when(curatorTransactionBridge.and()).thenReturn(curatorTransactionFinal);
        Map<String, List<Integer>> shardingItems = new HashMap<>(1);
        shardingItems.put("host0", Arrays.asList(0, 1, 2));
        ShardingService.PersistShardingInfoTransactionExecutionCallback actual = shardingService.new PersistShardingInfoTransactionExecutionCallback(shardingItems);
        actual.execute(curatorTransactionFinal);
        verify(curatorTransactionFinal).create();
        verify(transactionCreateBuilder).forPath("/testJob/servers/host0/sharding", "0,1,2".getBytes());
        verify(curatorTransactionFinal, times(2)).delete();
        verify(transactionDeleteBuilder).forPath("/testJob/leader/sharding/necessary");
        verify(transactionDeleteBuilder).forPath("/testJob/leader/sharding/processing");
        verify(curatorTransactionBridge, times(3)).and();
    }
}
