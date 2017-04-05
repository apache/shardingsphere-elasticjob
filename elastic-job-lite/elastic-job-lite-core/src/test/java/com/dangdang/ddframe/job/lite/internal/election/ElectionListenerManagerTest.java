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
import com.dangdang.ddframe.job.lite.internal.election.ElectionListenerManager.LeaderElectionJobListener;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.server.ServerStatus;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ElectionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ServerService serverService;
    
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(null, "test_job");
    
    @BeforeClass
    public static void setUpJobInstance() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
    }
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(electionListenerManager, electionListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverService", serverService);
    }
    
    @Test
    public void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.<LeaderElectionJobListener>any());
    }
    
    @Test
    public void assertIsNotLeaderInstancePathAndServerPath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/other", Type.NODE_REMOVED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenAddLeaderInstancePath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_ADDED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithoutAvailableServers() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_REMOVED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServer() {
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_REMOVED, "127.0.0.1");
        verify(leaderService).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenServerDisableWithoutLeader() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_REMOVED, ServerStatus.DISABLED.name());
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenServerEnableWithLeader() {
        when(leaderService.hasLeader()).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_UPDATED, "");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenServerEnableWithoutLeader() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_UPDATED, "");
        verify(leaderService).electLeader();
    }
    
    @Test
    public void assertLeaderRemoveWhenFollowerDisable() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_UPDATED, ServerStatus.DISABLED.name());
        verify(leaderService, times(0)).removeLeader();
    }
    
    @Test
    public void assertLeaderRemoveWhenLeaderDisable() {
        when(leaderService.isLeader()).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_UPDATED, ServerStatus.DISABLED.name());
        verify(leaderService).removeLeader();
    }
}
