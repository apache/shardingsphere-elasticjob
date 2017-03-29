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

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
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
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("mockedIP@-@0"));
        InstanceNode instanceNode = new InstanceNode("test_job");
        ServerNode serverNode = new ServerNode("test_job");
        ReflectionUtils.setFieldValue(serverNode, "ip", "mockedIP");
        ReflectionUtils.setFieldValue(serverService, "instanceNode", instanceNode);
        ReflectionUtils.setFieldValue(serverService, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(serverService, "localHostService", localHostService);
        when(localHostService.getIp()).thenReturn("mockedIP");
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", ServerStatus.DISABLED.name());
        verify(jobNodeStorage).fillEphemeralJobNode("instances/mockedIP@-@0", InstanceStatus.READY.name());
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", "");
        verify(jobNodeStorage).fillEphemeralJobNode("instances/mockedIP@-@0", InstanceStatus.READY.name());
    }
    
    @Test
    public void assertProcessServerShutdown() {
        serverService.removeInstanceStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("instances/mockedIP@-@0");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateInstanceStatus(InstanceStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("instances/mockedIP@-@0", InstanceStatus.RUNNING.name());
    }
    
    @Test
    public void assertRemoveServerStatus() {
        serverService.removeInstanceStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("instances/mockedIP@-@0");
    }
    
    @Test
    public void assertGetAvailableShardingServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Arrays.asList("host0@-@0", "host2@-@0", "host3@-@0", "host4@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/host2")).thenReturn(ServerStatus.DISABLED.name());
        assertThat(serverService.getAvailableShardingUnits(), is(Arrays.asList(new JobInstance("host0@-@0"), new JobInstance("host3@-@0"), new JobInstance("host4@-@0"))));
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Arrays.asList("host0@-@0", "host2@-@0", "host3@-@0"));
        when(jobNodeStorage.getJobNodeData("servers/host2")).thenReturn(ServerStatus.DISABLED.name());
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("mockedIP@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn(ServerStatus.DISABLED.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("mockedIP@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn("");
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("mockedIP@-@0"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("servers/mockedIP/instances/mockedIP@-@0")).thenReturn(InstanceStatus.RUNNING.name());
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/mockedIP@-@0/status")).thenReturn(InstanceStatus.RUNNING.name());
        assertFalse(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.getJobNodeChildrenKeys("instances")).thenReturn(Collections.singletonList("mockedIP@-@0"));
        when(jobNodeStorage.isJobNodeExisted("instances/mockedIP@-@0")).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly("instances/mockedIP@-@0")).thenReturn(InstanceStatus.READY.name());
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn("");
        assertTrue(serverService.isLocalhostServerReady());
    }
    
    @Test
    public void assertIsServerEnabled() {
        when(jobNodeStorage.getJobNodeData("servers/mockedIP")).thenReturn("");
        assertTrue(serverService.isServerEnabled("mockedIP"));
    }
}
