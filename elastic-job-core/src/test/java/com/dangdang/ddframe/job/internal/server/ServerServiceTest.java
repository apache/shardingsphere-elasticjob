/**
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

package com.dangdang.ddframe.job.internal.server;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.integrate.AbstractBaseStdJobTest.TestJob;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

public final class ServerServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    private final JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private final ServerService serverService = new ServerService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(serverService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(serverService, "localHostService", localHostService);
        ReflectionUtils.setFieldValue(serverService, "leaderElectionService", leaderElectionService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        jobConfig.setOverwrite(true);
    }
    
    @Test
    public void assertPersistServerOnlineWhenOverwriteDisabled() {
        when(leaderElectionService.hasLeader()).thenReturn(true);
        jobConfig.setOverwrite(false);
        serverService.persistServerOnline();
        verify(leaderElectionService).hasLeader();
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite("servers/mockedIP/hostName", "mockedHostName");
        verify(localHostService, times(2)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage).getJobConfiguration();
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/status", ServerStatus.READY);
    }
    
    @Test
    public void assertPersistServerOnlineForDisabledServerWithLeaderElecting() {
        when(leaderElectionService.hasLeader()).thenReturn(false);
        jobConfig.setDisabled(true);
        serverService.persistServerOnline();
        verify(leaderElectionService).hasLeader();
        verify(leaderElectionService).leaderElection();
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite("servers/mockedIP/hostName", "mockedHostName");
        verify(localHostService, times(3)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage, times(2)).getJobConfiguration();
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite("servers/mockedIP/disabled", "");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/status", ServerStatus.READY);
    }
    
    @Test
    public void assertPersistServerOnlineForEnabledServer() {
        when(leaderElectionService.hasLeader()).thenReturn(true);
        serverService.persistServerOnline();
        verify(leaderElectionService).hasLeader();
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite("servers/mockedIP/hostName", "mockedHostName");
        verify(localHostService, times(3)).getIp();
        verify(localHostService).getHostName();
        verify(jobNodeStorage, times(2)).getJobConfiguration();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).fillEphemeralJobNode("servers/mockedIP/status", ServerStatus.READY);
    }
    
    @Test
    public void assertClearJobStopedStatus() {
        serverService.clearJobStopedStatus();
        verify(jobNodeStorage).removeJobNodeIfExisted("servers/mockedIP/stoped");
    }
    
    @Test
    public void assertIsJobStopedManually() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/stoped")).thenReturn(true);
        assertTrue(serverService.isJobStopedManually());
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/stoped");
    }
    
    @Test
    public void assertUpdateServerStatus() {
        serverService.updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).updateJobNode("servers/mockedIP/status", ServerStatus.RUNNING);
    }
    
    @Test
    public void assertGetAllServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        assertThat(serverService.getAllServers(), is(Arrays.asList("host0", "host1", "host2", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
    }
    
    @Test
    public void assertGetAvailableServers() {
        when(jobNodeStorage.getJobNodeChildrenKeys("servers")).thenReturn(Arrays.asList("host0", "host2", "host1", "host3"));
        when(jobNodeStorage.isJobNodeExisted("servers/host0/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host0/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host1/disabled")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host2/status")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/status")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("servers/host3/disabled")).thenReturn(false);
        assertThat(serverService.getAvailableServers(), is(Arrays.asList("host0", "host3")));
        verify(jobNodeStorage).getJobNodeChildrenKeys("servers");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host0/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host1/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/host2/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/status");
        verify(jobNodeStorage).isJobNodeExisted("servers/host3/disabled");
    }
    
    @Test
    public void assertIsServerReadyWhenServerDisabled() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(true);
        assertFalse(serverService.isServerReady());
        verify(localHostService).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
    }
    
    @Test
    public void assertIsServerReadyWhenServerStoped() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/stoped")).thenReturn(true);
        assertFalse(serverService.isServerReady());
        verify(localHostService, times(2)).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/stoped");
    }
    
    @Test
    public void assertIsServerReadyWhenServerCrashed() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/stoped")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(false);
        assertFalse(serverService.isServerReady());
        verify(localHostService, times(3)).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/stoped");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
    }
    
    @Test
    public void assertIsServerReadyWhenServerRunning() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/stoped")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/status")).thenReturn("RUNNING");
        assertFalse(serverService.isServerReady());
        verify(localHostService, times(3)).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/stoped");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/status");
    }
    
    @Test
    public void assertIsServerReadyWhenServerReady() {
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/disabled")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/stoped")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("servers/mockedIP/status")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("servers/mockedIP/status")).thenReturn("READY");
        assertTrue(serverService.isServerReady());
        verify(localHostService, times(3)).getIp();
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/disabled");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/stoped");
        verify(jobNodeStorage).isJobNodeExisted("servers/mockedIP/status");
        verify(jobNodeStorage).getJobNodeData("servers/mockedIP/status");
    }
    
    @Test
    public void assertPersistProcessSuccessCount() {
        serverService.persistProcessSuccessCount(100);
        verify(jobNodeStorage).replaceJobNode("servers/mockedIP/processSuccessCount", 100);
        verify(localHostService).getIp();
    }
    
    @Test
    public void assertPersistProcessFailureCount() {
        serverService.persistProcessFailureCount(10);
        verify(jobNodeStorage).replaceJobNode("servers/mockedIP/processFailureCount", 10);
        verify(localHostService).getIp();
    }
}
