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

import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
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
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(serverService, "localHostService", localHostService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
    }
    
    @Test
    public void assertClearPreviousServerStatus() {
        serverService.clearPreviousServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted(ServerNode.getStatusNode("mockedIP"));
        verify(jobNodeStorage).removeJobNodeIfExisted(ServerNode.getShutdownNode("mockedIP"));
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP/hostName", "mockedHostName");
        verify(localHostService, times(4)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage).fillJobNode("servers/mockedIP/disabled", "");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/status", ServerStatus.READY);
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/shutdown");
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP/hostName", "mockedHostName");
        verify(localHostService, times(4)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/status", ServerStatus.READY);
    }
    
    @Test
    public void assertClearJobTriggerStatus() {
        serverService.clearJobTriggerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/trigger");
    }
    
    @Test
    public void assertClearJobPausedStatus() {
        serverService.clearJobPausedStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/paused");
    }
    
    @Test
    public void assertIsJobPausedManually() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(true);
        assertTrue(serverService.isJobPausedManually());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
    }
    
    @Test
    public void assertProcessServerShutdown() {
        serverService.processServerShutdown();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/status");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("servers/mockedIP/status", ServerStatus.RUNNING);
    }
    
    @Test
    public void assertRemoveServerStatus() {
        serverService.removeServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/status");
    }
    
    @Test
    public void assertGetAllServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        assertThat(serverService.getAllServers(), is(Arrays.asList("host0", "host1", "host2", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
    }
    
    @Test
    public void assertGetAvailableShardingServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/paused")).thenReturn(true);
        assertThat(serverService.getAvailableShardingServers(), is(Arrays.asList("host0", "host3", "host4")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/disabled");
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/paused")).thenReturn(true);
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/paused");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerPaused() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/shutdown")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/shutdown");
    }
        
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/status")).thenReturn("RUNNING");
        assertFalse(serverService.isLocalhostServerReady());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/status")).thenReturn("READY");
        assertTrue(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/status");
    }
    
    @Test
    public void assertIsLocalhostServerEnabled() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        assertTrue(serverService.isLocalhostServerEnabled());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
    }
    
    @Test
    public void assertHasStatusNode() {
        when(jobNodeStorage.isJobNodeExisted(ServerNode.getStatusNode("IP"))).thenReturn(true);
        serverService.hasStatusNode("IP");
        verify(jobNodeStorage).isJobNodeExisted(ServerNode.getStatusNode("IP"));
    }
}
