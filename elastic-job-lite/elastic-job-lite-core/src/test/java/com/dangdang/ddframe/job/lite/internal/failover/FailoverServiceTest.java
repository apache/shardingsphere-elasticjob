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

import com.dangdang.ddframe.job.lite.internal.failover.FailoverService.FailoverLeaderExecutionCallback;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class FailoverServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final FailoverService failoverService = new FailoverService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(failoverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverService, "localHostService", localHostService);
        ReflectionUtils.setFieldValue(failoverService, "serverService", serverService);
        ReflectionUtils.setFieldValue(failoverService, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverService, "jobName", "test_job");
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
    }
    
    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsNotAssigned() {
        when(jobNodeStorage.isJobNodeExisted("execution/0/failover")).thenReturn(true);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("execution/0/failover");
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("leader/failover/items/0");
    }
    
    @Test
    public void assertSetCrashedFailoverFlagWhenItemIsAssigned() {
        when(jobNodeStorage.isJobNodeExisted("execution/0/failover")).thenReturn(false);
        failoverService.setCrashedFailoverFlag(0);
        verify(jobNodeStorage).isJobNodeExisted("execution/0/failover");
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/failover/items/0");
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeNotExisted() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), Matchers.<FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenItemsRootNodeIsEmpty() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Collections.<String>emptyList());
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), Matchers.<FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfUnnecessaryWhenServerIsNotReady() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        when(serverService.isLocalhostServerReady()).thenReturn(false);
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(serverService).isLocalhostServerReady();
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/failover/latch"), Matchers.<FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverIfNecessary() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        when(serverService.isLocalhostServerReady()).thenReturn(true);
        failoverService.failoverIfNecessary();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage).getJobNodeChildrenKeys("leader/failover/items");
        verify(serverService).isLocalhostServerReady();
        verify(jobNodeStorage).executeInLeader(eq("leader/failover/latch"), Matchers.<FailoverLeaderExecutionCallback>any());
    }
    
    @Test
    public void assertFailoverLeaderExecutionCallbackIfNotNecessary() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(false);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(0)).getJobNodeChildrenKeys("leader/failover/items");
    }
    
    @Test
    public void assertFailoverLeaderExecutionCallbackIfNecessary() {
        when(jobNodeStorage.isJobNodeExisted("leader/failover/items")).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("leader/failover/items")).thenReturn(Arrays.asList("0", "1", "2"));
        when(serverService.isLocalhostServerReady()).thenReturn(true);
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        failoverService.new FailoverLeaderExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/failover/items");
        verify(jobNodeStorage, times(2)).getJobNodeChildrenKeys("leader/failover/items");
        verify(serverService).isLocalhostServerReady();
        verify(jobNodeStorage).fillEphemeralJobNode("execution/0/failover", "mockedIP");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/failover/items/0");
        verify(jobScheduleController).triggerJob();
    }
    
    @Test
    public void assertUpdateFailoverComplete() {
        failoverService.updateFailoverComplete(Arrays.asList(0, 1));
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/failover");
    }
    
    @Test
    public void assertGetLocalHostFailoverItems() {
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("execution/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/2/failover")).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("execution/0/failover")).thenReturn("mockedIP");
        when(jobNodeStorage.getJobNodeDataDirectly("execution/1/failover")).thenReturn("otherIP");
        assertThat(failoverService.getLocalHostFailoverItems(), is(Collections.singletonList(0)));
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("execution/0/failover");
        verify(jobNodeStorage).getJobNodeDataDirectly("execution/1/failover");
    }
    
    @Test
    public void assertGetLocalHostTakeOffItems() {
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1, 2));
        when(jobNodeStorage.isJobNodeExisted("execution/0/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/1/failover")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/2/failover")).thenReturn(false);
        assertThat(failoverService.getLocalHostTakeOffItems(), is(Arrays.asList(0, 1)));
        verify(shardingService).getLocalHostShardingItems();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/failover");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/failover");
        verify(jobNodeStorage).isJobNodeExisted("execution/2/failover");
    }
    
    @Test
    public void assertRemoveFailoverInfo() {
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        failoverService.removeFailoverInfo();
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/failover");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/failover");
    }
}
