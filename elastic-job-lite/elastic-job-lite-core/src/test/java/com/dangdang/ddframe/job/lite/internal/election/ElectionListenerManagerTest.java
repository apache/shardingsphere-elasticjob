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

import com.dangdang.ddframe.job.lite.internal.election.ElectionListenerManager.LeaderElectionJobListener;
import com.dangdang.ddframe.job.lite.internal.server.ServerNode;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ElectionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerNode serverNode;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ServerService serverService;
    
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(electionListenerManager, electionListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverNode", serverNode);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverService", serverService);
    }
    
    @Test
    public void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<LeaderElectionJobListener>any());
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsNotLeaderHostPath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/leader/election/other", null, "localhost".getBytes())), "/test_job/leader/election/other");
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsLeaderHostPathButNotRemove() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/leader/election/host", null, "localhost".getBytes())), "/test_job/leader/election/host");
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsLeaderHostPathAndIsRemoveAndIsLeader() {
        when(leaderElectionService.hasLeader()).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/leader/election/host", null, "localhost".getBytes())), "/test_job/leader/election/host");
        verify(leaderElectionService).hasLeader();
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsLeaderHostPathAndIsRemoveAndIsNotLeaderWithAvailableServers() {
        when(leaderElectionService.hasLeader()).thenReturn(false);
        when(serverService.getAvailableServers()).thenReturn(Collections.singletonList("localhost"));
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/leader/election/host", null, "localhost".getBytes())), "/test_job/leader/election/host");
        verify(leaderElectionService).hasLeader();
        verify(serverService).getAvailableServers();
        verify(leaderElectionService).leaderElection();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsLeaderHostPathAndIsRemoveAndIsNotLeaderWithoutAvailableServers() {
        when(leaderElectionService.hasLeader()).thenReturn(false);
        when(serverService.getAvailableServers()).thenReturn(Collections.<String>emptyList());
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/test_job/leader/election/host", null, "localhost".getBytes())), "/test_job/leader/election/host");
        verify(leaderElectionService).hasLeader();
        verify(serverService).getAvailableServers();
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenJobDisabledAndIsNotLeader() {
        when(leaderElectionService.isLeader()).thenReturn(false);
        when(serverNode.isLocalJobPausedPath("/test_job/server/mockedIP/disabled")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/server/mockedIP/disabled", null, "localhost".getBytes())), "/test_job/server/mockedIP/disabled");
        verify(leaderElectionService, times(0)).removeLeader();
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenJobShutdownAndIsLeader() {
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(serverNode.isLocalJobPausedPath("/test_job/server/mockedIP/shutdown")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/server/mockedIP/shutdown", null, "localhost".getBytes())), "/test_job/server/mockedIP/shutdown");
        verify(leaderElectionService).removeLeader();
    }
}
