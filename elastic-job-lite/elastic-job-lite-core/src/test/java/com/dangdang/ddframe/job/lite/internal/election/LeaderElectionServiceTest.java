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

package com.dangdang.ddframe.job.lite.internal.election;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService.LeaderElectionExecutionCallback;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class LeaderElectionServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerService serverService;
    
    private final LeaderElectionService leaderElectionService = new LeaderElectionService(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(leaderElectionService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(leaderElectionService, "serverService", serverService);
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
    }
    
    @Test
    public void assertLeaderForceElection() {
        leaderElectionService.leaderForceElection();
        verify(jobNodeStorage).executeInLeader(eq("leader/election/latch"), Matchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertLeaderElection() {
        leaderElectionService.leaderElection();
        verify(jobNodeStorage).executeInLeader(eq("leader/election/latch"), Matchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertLeaderElectionExecutionCallbackWithLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(true);
        leaderElectionService.new LeaderElectionExecutionCallback(false).execute();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
    
    @Test
    public void assertLeaderElectionExecutionCallbackWithoutLeaderAndIsAvailableServer() {
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        leaderElectionService.new LeaderElectionExecutionCallback(false).execute();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
    
    @Test
    public void assertLeaderElectionExecutionCallbackWithoutLeaderAndIsNotAvailableServer() {
        leaderElectionService.new LeaderElectionExecutionCallback(false).execute();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
    
    @Test
    public void assertLeaderForceElectionExecutionCallbackWithoutLeaderAndIsNotAvailableServer() {
        leaderElectionService.new LeaderElectionExecutionCallback(true).execute();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
    
    @Test
    public void assertIsLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(false, true);
        when(serverService.getAvailableServers()).thenReturn(Collections.singletonList("127.0.0.1"));
        when(jobNodeStorage.getJobNodeData("leader/election/instance")).thenReturn("127.0.0.1@-@0");
        assertTrue(leaderElectionService.isLeaderUntilBlock());
    }
    
    @Test
    public void assertIsNotLeader() {
        when(serverService.getAvailableServers()).thenReturn(Collections.<String>emptyList());
        assertFalse(leaderElectionService.isLeaderUntilBlock());
    }
    
    @Test
    public void assertHasLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(true);
        assertTrue(leaderElectionService.hasLeader());
    }
    
    @Test
    public void assertRemoveLeader() {
        leaderElectionService.removeLeader();
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/election/instance");
    }
}
