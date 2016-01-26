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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.integrate.AbstractBaseStdJobTest.TestJob;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService.LeaderElectionExecutionCallback;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;

public final class LeaderElectionServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    private final JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private final LeaderElectionService leaderElectionService = new LeaderElectionService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(leaderElectionService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(leaderElectionService, "localHostService", localHostService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        jobConfig.setOverwrite(true);
    }
    
    @Test
    public void assertLeaderElection() {
        leaderElectionService.leaderElection();
        verify(jobNodeStorage).executeInLeader(eq("leader/election/latch"), Matchers.<LeaderElectionExecutionCallback>any());
    }
    
    @Test
    public void assertLeaderElectionExecutionCallbackWithLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/host")).thenReturn(true);
        leaderElectionService.new LeaderElectionExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/election/host");
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode("leader/election/host", "mockedIP");
    }
    
    @Test
    public void assertLeaderElectionExecutionCallbackWithoutLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/host")).thenReturn(false);
        leaderElectionService.new LeaderElectionExecutionCallback().execute();
        verify(jobNodeStorage).isJobNodeExisted("leader/election/host");
        verify(jobNodeStorage).fillEphemeralJobNode("leader/election/host", "mockedIP");
    }
    
    @Test
    public void assertIsLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/host")).thenReturn(false, true);
        when(jobNodeStorage.getJobNodeData("leader/election/host")).thenReturn("mockedIP");
        assertTrue(leaderElectionService.isLeader());
    }
    
    @Test
    public void assertHasLeader() {
        when(jobNodeStorage.isJobNodeExisted("leader/election/host")).thenReturn(true);
        assertTrue(leaderElectionService.hasLeader());
    }
}
