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
    
    private ServerNode serverNode;
    
    private ServerOperationNode serverOperationNode;
    
    private final ServerService serverService = new ServerService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        JobRegistry.getInstance().addJobInstanceId("test_job", "test_job_instance_id");
        serverNode = new ServerNode("test_job");
        serverOperationNode = new ServerOperationNode("test_job");
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
    public void assertClearPreviousServerStatus() {
        serverService.clearPreviousServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted(serverNode.getStatusNode());
        verify(jobNodeStorage).removeJobNodeIfExisted(serverNode.getShutdownNode("mockedIP"));
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        serverService.persistServerOnline(false);
        verify(jobNodeStorage).fillJobNode("servers/mockedIP/operation/disabled", "");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/instances/test_job_instance_id/status", ServerStatus.READY);
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/test_job_instance_id/shutdown");
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        serverService.persistServerOnline(true);
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/operation/disabled");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/instances/test_job_instance_id/status", ServerStatus.READY);
    }
    
    @Test
    public void assertClearJobTriggerStatus() {
        serverService.clearJobTriggerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/test_job_instance_id/trigger");
    }
    
    @Test
    public void assertProcessServerShutdown() {
        serverService.processServerShutdown();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/test_job_instance_id/status");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("servers/mockedIP/instances/test_job_instance_id/status", ServerStatus.RUNNING);
    }
    
    @Test
    public void assertRemoveServerStatus() {
        serverService.removeServerStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/instances/test_job_instance_id/status");
    }
    
    @Test
    public void assertGetAllShardingUnits() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Arrays.asList("test_job_instance_id_1", "test_job_instance_id_2"));
        assertThat(serverService.getAllShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id_1"), new JobShardingUnit("host0", "test_job_instance_id_2"),
                new JobShardingUnit("host1", "test_job_instance_id_1"), new JobShardingUnit("host1", "test_job_instance_id_2"),
                new JobShardingUnit("host2", "test_job_instance_id_1"), new JobShardingUnit("host2", "test_job_instance_id_2"),
                new JobShardingUnit("host3", "test_job_instance_id_1"), new JobShardingUnit("host3", "test_job_instance_id_2"))));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host0/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host1/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host2/instances");
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers/host3/instances");
    }
    
    @Test
    public void assertGetAvailableShardingServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3", "host4"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host4/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/operation/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/instances/test_job_instance_id/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/instances/test_job_instance_id/status")).thenReturn(true);
        assertThat(serverService.getAvailableShardingUnits(), is(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id"), new JobShardingUnit("host3", "test_job_instance_id"), new JobShardingUnit("host4", "test_job_instance_id"))));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host4/operation/disabled");
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host0/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host1/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host2/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/host3/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/operation/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/instances/test_job_instance_id/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host4/instances/test_job_instance_id/status")).thenReturn(true);
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/operation/disabled");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerCrashed() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(false);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerDisabled() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/operation/disabled");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerShutdown() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown")).thenReturn(true);
        assertFalse(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown");
    }
        
    @Test
    public void assertIsLocalhostServerReadyWhenServerRunning() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/test_job_instance_id/status")).thenReturn("RUNNING");
        assertFalse(serverService.isLocalhostServerReady());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status");
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/instances/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerReadyWhenServerReady() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers/mockedIP/instances")).thenReturn(Collections.singletonList("test_job_instance_id"));
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/instances/test_job_instance_id/status")).thenReturn("READY");
        assertTrue(serverService.isLocalhostServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/operation/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/instances/test_job_instance_id/shutdown");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/instances/test_job_instance_id/status");
    }
    
    @Test
    public void assertIsLocalhostServerEnabled() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/operation/disabled")).thenReturn(false);
        assertTrue(serverService.isLocalhostServerEnabled());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/operation/disabled");
    }
    
    @Test
    public void assertIsOffline() {
        when(jobNodeStorage.isJobNodeExisted(ServerNode.getStatusNode("ip1", "test_job_instance_id"))).thenReturn(true);
        assertFalse(serverService.isOffline("ip1", "test_job_instance_id"));
        verify(jobNodeStorage).isJobNodeExisted(ServerNode.getStatusNode("ip1", "test_job_instance_id"));
    }
}
