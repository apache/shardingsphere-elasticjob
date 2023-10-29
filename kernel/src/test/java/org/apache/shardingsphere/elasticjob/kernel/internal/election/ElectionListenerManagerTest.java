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

package org.apache.shardingsphere.elasticjob.kernel.internal.election;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectionListenerManagerTest {
    
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
    
    @BeforeEach
    void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        ReflectionUtils.setSuperclassFieldValue(electionListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(electionListenerManager, "serverService", serverService);
    }
    
    @Test
    void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<ElectionListenerManager.LeaderElectionJobListener>any());
    }
    
    @Test
    void assertIsNotLeaderInstancePathAndServerPath() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.DELETED, "/test_job/leader/election/other", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenAddLeaderInstancePath() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenRemoveLeaderInstancePathWithoutAvailableServers() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServerButJobInstanceIsShutdown() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenRemoveLeaderInstancePathWithAvailableServer() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(serverService.isAvailableServer("127.0.0.1")).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/leader/election/instance", "127.0.0.1"));
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertLeaderElectionWhenServerDisableWithoutLeader() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenServerEnableWithLeader() {
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ""));
        verify(leaderService, times(0)).electLeader();
    }
    
    @Test
    void assertLeaderElectionWhenServerEnableWithoutLeader() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        electionListenerManager.new LeaderElectionJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ""));
        verify(leaderService).electLeader();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertLeaderAbdicationWhenFollowerDisable() {
        electionListenerManager.new LeaderAbdicationJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService, times(0)).removeLeader();
    }
    
    @Test
    void assertLeaderAbdicationWhenLeaderDisable() {
        when(leaderService.isLeader()).thenReturn(true);
        electionListenerManager.new LeaderAbdicationJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/servers/127.0.0.1", ServerStatus.DISABLED.name()));
        verify(leaderService).removeLeader();
    }
}
