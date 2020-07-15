/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.election;

import org.apache.curator.framework.recipes.cache.CuratorCacheListener.Type;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ElectionListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ServerService serverService;
    
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(null, "test_job");
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        ReflectionUtils.setSuperclassFieldValue(electionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverService", serverService);
    }
    
    @Test
    public void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<ElectionListenerManager.LeaderElectionJobListener>any());
    }
    
    @Test
    public void assertIsNotLeaderInstancePathAndServerPath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/other", Type.NODE_DELETED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenAddLeaderInstancePath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_CREATED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithoutAvailableServers() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_DELETED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServerButJobInstanceIsShutdown() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_DELETED, "127.0.0.1");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/leader/election/instance", Type.NODE_DELETED, "127.0.0.1");
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertLeaderElectionWhenServerDisableWithoutLeader() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_DELETED, ServerStatus.DISABLED.name());
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenServerEnableWithLeader() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_CHANGED, "");
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    public void assertLeaderElectionWhenServerEnableWithoutLeader() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        electionListenerManager.new LeaderElectionJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_CHANGED, "");
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertLeaderAbdicationWhenFollowerDisable() {
        electionListenerManager.new LeaderAbdicationJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_CHANGED, ServerStatus.DISABLED.name());
        verify(leaderService, times(0)).removeLeader();
    }
    
    @Test
    public void assertLeaderAbdicationWhenLeaderDisable() {
        when(leaderService.isLeader()).thenReturn(true);
        electionListenerManager.new LeaderAbdicationJobListener().dataChanged("/test_job/servers/127.0.0.1", Type.NODE_CHANGED, ServerStatus.DISABLED.name());
        verify(leaderService).removeLeader();
    }
}
