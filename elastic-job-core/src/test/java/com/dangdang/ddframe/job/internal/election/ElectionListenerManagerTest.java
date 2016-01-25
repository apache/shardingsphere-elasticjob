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

package com.dangdang.ddframe.job.internal.election;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest.TestJob;
import com.dangdang.ddframe.job.internal.election.ElectionListenerManager.LeaderElectionJobListener;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

public final class ElectionListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ShardingService shardingService;
    
    private final ElectionListenerManager electionListenerManager = new ElectionListenerManager(null, new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(electionListenerManager, electionListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
        ReflectionUtils.setFieldValue(electionListenerManager, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(electionListenerManager, "shardingService", shardingService);
    }
    
    @Test
    public void assertStart() {
        electionListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<LeaderElectionJobListener>any());
    }
    
    @Test
    public void assertLeaderElectionJobListenerWhenIsNotLeaderHostPath() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/leader/election/other", null, "localhost".getBytes())), "/testJob/leader/election/other");
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsLeaderHostPathButNotRemove() {
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/leader/election/host", null, "localhost".getBytes())), "/testJob/leader/election/host");
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsLeaderHostPathAndIsRemoveAndIsLeader() {
        when(leaderElectionService.hasLeader()).thenReturn(true);
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/leader/election/host", null, "localhost".getBytes())), "/testJob/leader/election/host");
        verify(leaderElectionService).hasLeader();
        verify(leaderElectionService, times(0)).leaderElection();
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsLeaderHostPathAndIsRemoveAndIsNotLeader() {
        when(leaderElectionService.hasLeader()).thenReturn(false);
        electionListenerManager.new LeaderElectionJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/testJob/leader/election/host", null, "localhost".getBytes())), "/testJob/leader/election/host");
        verify(leaderElectionService).hasLeader();
        verify(leaderElectionService).leaderElection();
        verify(shardingService).setReshardingFlag();
    }
    
//    @Test
//    public void assertListenLeaderElection() {
//        getRegistryCenter().persist("/testJob/leader/election/host", "host0");
//        assertFalse(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
//        WaitingUtils.waitingShortTime();
//        getRegistryCenter().remove("/testJob/leader/election");
//        WaitingUtils.waitingShortTime();
//        assertThat(getRegistryCenter().get("/testJob/leader/election/host"), is(localHostService.getIp()));
//        assertTrue(getRegistryCenter().isExisted("/testJob/leader/sharding/necessary"));
//    }
}
