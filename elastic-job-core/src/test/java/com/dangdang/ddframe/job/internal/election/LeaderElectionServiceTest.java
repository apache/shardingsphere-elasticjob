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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.FakeLocalHostService;
import com.dangdang.ddframe.test.WaitingUtils;

public final class LeaderElectionServiceTest extends AbstractBaseJobTest {
    
    private final LeaderElectionService leaderElectionService = new LeaderElectionService(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(leaderElectionService, "localHostService", new FakeLocalHostService("host0"));
    }
    
    @Test
    public void assertLeaderElection() {
        leaderElectionService.leaderElection();
        assertThat(getRegistryCenter().getDirectly("/testJob/leader/election/host"), is("host0"));
        LeaderElectionService otherLeaderElectionService = new LeaderElectionService(getRegistryCenter(), getJobConfig());
        otherLeaderElectionService.leaderElection();
        assertThat(getRegistryCenter().getDirectly("/testJob/leader/election/host"), is("host0"));
    }
    
    @Test
    public void assertIsLeader() {
        new Thread() {
            
            @Override
            public void run() {
                WaitingUtils.waitingShortTime();
                getRegistryCenter().persist("/testJob/leader/election/host", "host0");
            }
        }.start();
        assertTrue(leaderElectionService.isLeader());
        assertFalse(new LeaderElectionService(getRegistryCenter(), getJobConfig()).isLeader());
    }
    
    @Test
    public void assertHasLeader() {
        getRegistryCenter().persist("/testJob/leader/election/host", "host0");
        assertTrue(leaderElectionService.hasLeader());
    }
    
    @Test
    public void assertHasNotLeader() {
        assertFalse(leaderElectionService.hasLeader());
    }
}
