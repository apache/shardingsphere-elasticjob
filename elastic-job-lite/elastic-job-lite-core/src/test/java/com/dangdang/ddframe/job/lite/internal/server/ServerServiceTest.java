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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingUnit;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ServerServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    private final ServerService serverService = new ServerService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        JobRegistry.getInstance().addJobInstanceId("test_job", "127.0.0.1@-@0");
        ServerNode serverNode = new ServerNode("test_job");
        ServerOperationNode serverOperationNode = new ServerOperationNode("test_job");
        ReflectionUtils.setFieldValue(serverNode, "ip", "mockedIP");
        ReflectionUtils.setFieldValue(serverOperationNode, "ip", "mockedIP");
        ReflectionUtils.setFieldValue(serverService, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(serverService, "serverOperationNode", serverOperationNode);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(serverService, "localHostService", localHostService);
        ReflectionUtils.setFieldValue(serverService, "serverOperationNode", serverOperationNode);
        when(localHostService.getIp()).thenReturn("mockedIP");
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", ServerStatus.DISABLED.name());
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/instances/127.0.0.1@-@0", InstanceStatus.READY.name());
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", "");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/instances/127.0.0.1@-@0", InstanceStatus.READY.name());
    }
    
    @Test
    public void assertProcessServerShutdown() {
        serverService.removeInstanceStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/127.0.0.1@-@0");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateInstanceStatus(InstanceStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("servers/mockedIP/instances/127.0.0.1@-@0", InstanceStatus.RUNNING.name());
    }
    
    @Test
    public void assertRemoveServerStatus() {
        serverService.removeInstanceStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/127.0.0.1@-@0");
    }
    
    @Test
    public void assertGetAllShardingUnits() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Arrays.asList("127.0.0.1@-@1", "127.0.0.1@-@2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Arrays.asList("127.0.0.1@-@1", "127.0.0.1@-@2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Arrays.asList("127.0.0.1@-@1", "127.0.0.1@-@2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Arrays.asList("127.0.0.1@-@1", "127.0.0.1@-@2"));
        assertThat(serverService.getAllShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "127.0.0.1@-@1"), new JobShardingUnit("host0", "127.0.0.1@-@2"),
                new JobShardingUnit("host1", "127.0.0.1@-@1"), new JobShardingUnit("host1", "127.0.0.1@-@2"),
                new JobShardingUnit("host2", "127.0.0.1@-@1"), new JobShardingUnit("host2", "127.0.0.1@-@2"),
                new JobShardingUnit("host3", "127.0.0.1@-@1"), new JobShardingUnit("host3", "127.0.0.1@-@2"))));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host0/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host1/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host2/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host3/instances");
    }
    
    @Test
    public void assertGetAvailableShardingServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host4/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host0/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host0/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host1/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.getJobNodeData("servers/host1")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host2/instances/127.0.0.1@-@0")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host3/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host3/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host4/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        assertThat(serverService.getAvailableShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "127.0.0.1@-@0"), new JobShardingUnit("host3", "127.0.0.1@-@0"), new JobShardingUnit("host4", "127.0.0.1@-@0"))));
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Collections.<String>emptyList());
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host0/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host0/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/instances/127.0.0.1@-@0")).thenReturn(false);
        when(jobNodeStorage.getJobNodeData("servers/host1")).thenReturn(ServerStatus.DISABLED.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host2/instances/127.0.0.1@-@0")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/host3/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/host3/operation/disabled")).thenReturn(false);
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn("");
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/127.0.0.1@-@0/status")).thenReturn(InstanceStatus.RUNNING.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("127.0.0.1@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/instances/127.0.0.1@-@0")).thenReturn(InstanceStatus.READY.name());
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        assertTrue(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerEnabled() {
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn("");
        assertTrue(serverService.isLocalhostServerEnabled());
    }
    
    @Test
    public void assertIsOffline() {
        when(jobNodeStorage.isJobNodeExisted(ServerNode.getInstanceNode("ip1", "127.0.0.1@-@0"))).thenReturn(true);
        assertFalse(serverService.isOffline("ip1", "127.0.0.1@-@0"));
        verify(jobNodeStorage).isJobNodeExisted(ServerNode.getInstanceNode("ip1", "127.0.0.1@-@0"));
    }
}
