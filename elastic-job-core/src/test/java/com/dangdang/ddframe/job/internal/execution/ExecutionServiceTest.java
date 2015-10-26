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

package com.dangdang.ddframe.job.internal.execution;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.test.WaitingUtils;

public final class ExecutionServiceTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final LeaderElectionService leaderElectionService = new LeaderElectionService(getRegistryCenter(), getJobConfig());
    
    private final ExecutionService executionService = new ExecutionService(getRegistryCenter(), getJobConfig());
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJob("testJob", new TestJobScheduler());
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
        ProcessCountStatistics.reset("testJob");
    }
    
    @Test
    public void assertRegisterJobBeginWhenNotAssignAnyItem() {
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Collections.<Integer>emptyList());
        executionService.registerJobBegin(jobExecutionShardingContext);
        assertFalse(getRegistryCenter().isExisted("/testJob/servers/" + localHostService.getIp() + "/status"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/running"));
    }
    
    @Test
    public void assertRegisterJobBeginWhenNotMonitorExecution() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/running"));
    }
    
    @Test
    public void assertRegisterJobBegin() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", "");
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.RUNNING.name()));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/running"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/running"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/running"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/lastBeginTime"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/lastBeginTime"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/lastBeginTime"));
        assertThat(getRegistryCenter().get("/testJob/execution/0/nextFireTime"), is("0"));
        assertThat(getRegistryCenter().get("/testJob/execution/1/nextFireTime"), is("0"));
        assertThat(getRegistryCenter().get("/testJob/execution/2/nextFireTime"), is("0"));
    }
    
    @Test
    public void assertRegisterJobCompletedWhenNotMonitorExecution() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", ServerStatus.READY.name());
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        executionService.registerJobCompleted(jobExecutionShardingContext);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution"));
    }
    
    @Test
    public void assertRegisterJobCompleted() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/status", "");
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        executionService.registerJobCompleted(jobExecutionShardingContext);
        assertThat(getRegistryCenter().get("/testJob/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/running"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/running"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/lastCompleteTime"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/lastCompleteTime"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/lastCompleteTime"));
    }
    
    @Test
    public void assertMisfireIfNecessary() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        assertTrue(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertMisfireIfNotNecessary() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        assertFalse(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertMisfireIfNotNecessaryWhenNotMonitorExecution() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        assertFalse(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertSetNeedFixExecutionInfoFlag() {
        executionService.setNeedFixExecutionInfoFlag();
        assertTrue(getRegistryCenter().isExisted("/testJob/leader/execution/necessary"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNotMonitorExecution() {
        leaderElectionService.leaderElection();
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        executionService.cleanPreviousExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNotNeedFixExecutionInfo() {
        leaderElectionService.leaderElection();
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        executionService.cleanPreviousExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForNewValueGreater() {
        leaderElectionService.leaderElection();
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "4");
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persist("/testJob/leader/execution/necessary", "");
        executionService.cleanPreviousExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/3/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/3"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForNewValueLess() {
        leaderElectionService.leaderElection();
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "1");
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persist("/testJob/leader/execution/necessary", "");
        executionService.cleanPreviousExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForValueEquals() {
        leaderElectionService.leaderElection();
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "2");
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persist("/testJob/leader/execution/necessary", "");
        executionService.cleanPreviousExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1"));
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenIsNotLeaderAndIsCleaning() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/leader/election/host", "otherHost");
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persistEphemeral("/testJob/leader/execution/cleaning", "");
        new Thread() {
            
            @Override
            public void run() {
                WaitingUtils.waitingShortTime();
                getRegistryCenter().remove("/testJob/leader/execution/cleaning");
            }
        }.start();
        executionService.cleanPreviousExecutionInfo();
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/completed"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1"));
    }
    
    @Test
    public void assertClearRunningInfo() {
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/running", "");
        executionService.clearRunningInfo(Arrays.asList(0, 1, 2));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/running"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/completed"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/running"));
    }
    
    @Test
    public void assertSetMisfire() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/0/misfire"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/1/misfire"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/misfire"));
    }
    
    @Test
    public void assertSetMisfireWhenIsNotMonitorExecution() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/misfire"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/misfire"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/2/misfire"));
    }
    
    @Test
    public void assertGetMisfiredJobItems() {
        getRegistryCenter().persist("/testJob/execution/0/misfire", "");
        getRegistryCenter().persist("/testJob/execution/1/misfire", "");
        assertThat(executionService.getMisfiredJobItems(Arrays.asList(0, 1, 2)), is(Arrays.asList(0, 1)));
    }
    
    @Test
    public void assertClearMisfire() {
        getRegistryCenter().persist("/testJob/execution/0/misfire", "");
        getRegistryCenter().persist("/testJob/execution/1/misfire", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        executionService.clearMisfire(Arrays.asList(0, 1));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/0/misfire"));
        assertFalse(getRegistryCenter().isExisted("/testJob/execution/1/misfire"));
        assertTrue(getRegistryCenter().isExisted("/testJob/execution/2/misfire"));
    }
    
    @Test
    public void assertRemoveExecutionInfo() {
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        executionService.removeExecutionInfo();
        assertFalse(getRegistryCenter().isExisted("/testJob/execution"));
    }
    
    @Test
    public void assertHasRunningItemsForAll() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertTrue(executionService.hasRunningItems());
    }
    
    @Test
    public void assertNotHaveRunningItemsForAll() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertFalse(executionService.hasRunningItems());
    }
    
    @Test
    public void assertNotHaveRunningItemsWhenJNotMonitorExecutionForAll() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertFalse(executionService.hasRunningItems());
    }
    
    @Test
    public void assertHasRunningItems() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertTrue(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertNotHaveRunningItems() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertFalse(executionService.hasRunningItems(Arrays.asList(1, 2)));
    }
    
    @Test
    public void assertNotHaveRunningItemsWhenJNotMonitorExecution() {
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2/misfire", "");
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
    }
    
    @Test
    public void assertIsCompleted() {
        getRegistryCenter().persist("/testJob/execution/0/completed", "");
        assertTrue(executionService.isCompleted(0));
    }
    
    @Test
    public void assertIsNotCompleted() {
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        assertFalse(executionService.isCompleted(0));
    }
    
    class TestJobScheduler extends JobScheduler {
        
        public TestJobScheduler() {
            super(getRegistryCenter(), getJobConfig());
        }
        
        @Override
        public Date getNextFireTime() {
            return new Date(0L);
        }
    }
}
