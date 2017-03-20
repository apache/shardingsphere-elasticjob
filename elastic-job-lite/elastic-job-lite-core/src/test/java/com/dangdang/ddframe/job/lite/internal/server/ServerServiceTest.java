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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ServerServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    private ServerNode serverNode = new ServerNode("test_job");
    
    private final ServerService serverService = new ServerService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(serverService, "localHostService", localHostService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        JobRegistry.getInstance().addJobInstanceId("test_job", "test_job_instance_id");
    }
    
    @Test
    public void assertClearPreviousServerStatus() {
        serverService.clearPreviousServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted(serverNode.getStatusNode("mockedIP"));
        verify(jobNodeStorage).removeJobNodeIfExisted(serverNode.getShutdownNode("mockedIP"));
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", "mockedHostName");
        verify(localHostService, times(4)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage).fillJobNode("servers/mockedIP/test_job_instance_id/disabled", "");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/test_job_instance_id/status", ServerStatus.READY);
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/shutdown");
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP", "mockedHostName");
        verify(localHostService, times(4)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/disabled");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/test_job_instance_id/status", ServerStatus.READY);
    }
    
    @Test
    public void assertClearJobTriggerStatus() {
        serverService.clearJobTriggerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/trigger");
    }
    
    @Test
    public void assertClearJobPausedStatus() {
        serverService.clearJobPausedStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/paused");
    }
    
    @Test
    public void assertIsJobPausedManually() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(true);
        assertTrue(serverService.isJobPausedManually());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
    }
    
    @Test
    public void assertProcessServerShutdown() {
        serverService.processServerShutdown();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/status");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("servers/mockedIP/test_job_instance_id/status", ServerStatus.RUNNING);
    }
    
    @Test
    public void assertRemoveServerStatus() {
        serverService.removeServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/test_job_instance_id/status");
    }
    
    @Test
    public void assertGetAllShardingUnits() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        assertThat(serverService.getAllShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id_1"), new JobShardingUnit("host0", "test_job_instance_id_2"),
                new JobShardingUnit("host1", "test_job_instance_id_1"), new JobShardingUnit("host1", "test_job_instance_id_2"),
                new JobShardingUnit("host2", "test_job_instance_id_1"), new JobShardingUnit("host2", "test_job_instance_id_2"),
                new JobShardingUnit("host3", "test_job_instance_id_1"), new JobShardingUnit("host3", "test_job_instance_id_2"))));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host0");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host1");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host2");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host3");
    }
    
    @Test
    public void assertGetAvailableShardingServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host4")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/test_job_instance_id/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/test_job_instance_id/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/test_job_instance_id/paused")).thenReturn(true);
        assertThat(serverService.getAvailableShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id"), new JobShardingUnit("host3", "test_job_instance_id"), new JobShardingUnit("host4", "test_job_instance_id"))));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/test_job_instance_id/disabled");
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host4")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/test_job_instance_id/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/test_job_instance_id/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/test_job_instance_id/paused")).thenReturn(true);
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/test_job_instance_id/paused");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerPaused() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown");
    }
        
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/test_job_instance_id/status")).thenReturn("RUNNING");
        assertFalse(serverService.isLocalhostServerReady());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/status");
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/test_job_instance_id/status")).thenReturn("READY");
        assertTrue(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/paused");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerEnabled() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled")).thenReturn(false);
        assertTrue(serverService.isLocalhostServerEnabled());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/test_job_instance_id/disabled");
    }
    
    @Test
    public void assertIsOffline() {
        when(jobNodeStorage.isJobNodeExisted(ServerNode.getStatusNode("ip1", "test_job_instance_id"))).thenReturn(true);
        assertFalse(serverService.isOffline("ip1", "test_job_instance_id"));
        verify(jobNodeStorage).isJobNodeExisted(ServerNode.getStatusNode("ip1", "test_job_instance_id"));
    }
}
