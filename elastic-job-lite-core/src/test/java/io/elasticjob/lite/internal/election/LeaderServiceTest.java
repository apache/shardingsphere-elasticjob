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

package io.elasticjob.lite.internal.election;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.internal.election.LeaderService.LeaderElectionExecutionCallback;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.schedule.JobScheduleController;
import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class LeaderServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerService serverService;
    
    private LeaderService leaderService;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        leaderService = new LeaderService(null, "test_job");
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(leaderService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(leaderService, "serverService", serverService);
    }
    
    @Test
    public void assertElectLeader() {
        leaderService.electLeader();
        verify(jobNodeStorage).executeInLeader(eq("leader/election/latch"), ArgumentMatchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertIsLeaderUntilBlockWithLeader() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("leader/election/instance")).thenReturn("127.0.0.1@-@0");
        assertTrue(leaderService.isLeaderUntilBlock());
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/election/latch"), ArgumentMatchers.<LeaderElectionExecutionCallback>any());
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertIsLeaderUntilBlockWithoutLeaderAndAvailableServers() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(false);
        assertFalse(leaderService.isLeaderUntilBlock());
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/election/latch"), ArgumentMatchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertIsLeaderUntilBlockWithoutLeaderWithAvailableServers() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(false, true);
        assertFalse(leaderService.isLeaderUntilBlock());
        verify(jobNodeStorage, times(0)).executeInLeader(eq("leader/election/latch"), ArgumentMatchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertIsLeaderUntilBlockWhenHasLeader() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(false, true);
        when(serverService.hasAvailableServers()).thenReturn(true);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        when(jobNodeStorage.getJobNodeData("leader/election/instance")).thenReturn("127.0.0.1@-@0");
        assertTrue(leaderService.isLeaderUntilBlock());
        verify(jobNodeStorage).executeInLeader(eq("leader/election/latch"), ArgumentMatchers.<LeaderElectionExecutionCallback>any());
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertIsLeader() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        when(jobNodeStorage.getJobNodeData("leader/election/instance")).thenReturn("127.0.0.1@-@0");
        assertTrue(leaderService.isLeader());
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertHasLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(true);
        assertTrue(leaderService.hasLeader());
    }
    
    @Test
    public void assertRemoveLeader() {
        leaderService.removeLeader();
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/election/instance");
    }
    
    @Test
    public void assertElectLeaderExecutionCallbackWithLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/instance")).thenReturn(true);
        leaderService.new LeaderElectionExecutionCallback().execute();
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
    
    @Test
    public void assertElectLeaderExecutionCallbackWithoutLeader() {
        leaderService.new LeaderElectionExecutionCallback().execute();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/election/instance", "127.0.0.1@-@0");
    }
}
