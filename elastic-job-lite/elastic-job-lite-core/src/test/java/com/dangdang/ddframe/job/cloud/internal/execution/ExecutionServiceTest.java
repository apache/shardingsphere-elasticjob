/*
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

package com.dangdang.ddframe.job.cloud.internal.execution;

import com.dangdang.ddframe.job.cloud.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.cloud.fixture.TestJob;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.internal.env.LocalHostService;
import com.dangdang.ddframe.job.cloud.internal.server.ServerService;
import com.dangdang.ddframe.job.cloud.internal.server.ServerStatus;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.cloud.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobScheduleController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ExecutionServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private LocalHostService localHostService;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").overwrite(true).build();
    
    private final ExecutionService executionService = new ExecutionService(null, jobConfig);
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(executionService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionService, "configService", configService);
        ReflectionUtils.setFieldValue(executionService, "serverService", serverService);
        ReflectionUtils.setFieldValue(executionService, "leaderElectionService", leaderElectionService);
        when(localHostService.getIp()).thenReturn("mockedIP");
        when(localHostService.getHostName()).thenReturn("mockedHostName");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
    }
    
    @Test
    public void assertRegisterJobBeginWhenNotAssignAnyItem() {
        executionService.registerJobBegin(new JobExecutionMultipleShardingContext());
        verify(configService, times(0)).isMonitorExecution();
    }
    
    @Test
    public void assertRegisterJobBeginWhenNotMonitorExecution() {
        when(configService.isMonitorExecution()).thenReturn(false);
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        verify(configService).isMonitorExecution();
    }
    
    @Test
    public void assertRegisterJobBeginWithoutNextFireTime() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobScheduleController.getNextFireTime()).thenReturn(null);
        JobRegistry.getInstance().addJobScheduleController("testJob", jobScheduleController);
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        verify(configService).isMonitorExecution();
        verify(serverService).updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).fillEphemeralJobNode("execution/0/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("execution/1/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("execution/2/running", "");
        verify(jobNodeStorage).replaceJobNode(eq("execution/0/lastBeginTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/1/lastBeginTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/2/lastBeginTime"), anyLong());
    }
    
    @Test
    public void assertRegisterJobBeginWithNextFireTime() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobScheduleController.getNextFireTime()).thenReturn(new Date(0L));
        JobRegistry.getInstance().addJobScheduleController("testJob", jobScheduleController);
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobBegin(jobExecutionShardingContext);
        verify(configService).isMonitorExecution();
        verify(serverService).updateServerStatus(ServerStatus.RUNNING);
        verify(jobNodeStorage).fillEphemeralJobNode("execution/0/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("execution/1/running", "");
        verify(jobNodeStorage).fillEphemeralJobNode("execution/2/running", "");
        verify(jobNodeStorage).replaceJobNode(eq("execution/0/lastBeginTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/1/lastBeginTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/2/lastBeginTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode("execution/0/nextFireTime", 0L);
        verify(jobNodeStorage).replaceJobNode("execution/1/nextFireTime", 0L);
        verify(jobNodeStorage).replaceJobNode("execution/2/nextFireTime", 0L);
    }
    
    @Test
    public void assertRegisterJobCompletedWhenNotMonitorExecution() {
        when(configService.isMonitorExecution()).thenReturn(false);
        executionService.registerJobCompleted(new JobExecutionMultipleShardingContext());
        verify(configService).isMonitorExecution();
        verify(serverService, times(0)).updateServerStatus(ServerStatus.READY);
    }
    
    @Test
    public void assertRegisterJobCompleted() {
        when(configService.isMonitorExecution()).thenReturn(true);
        JobExecutionMultipleShardingContext jobExecutionShardingContext = new JobExecutionMultipleShardingContext();
        jobExecutionShardingContext.setShardingItems(Arrays.asList(0, 1, 2));
        executionService.registerJobCompleted(jobExecutionShardingContext);
        verify(serverService).updateServerStatus(ServerStatus.READY);
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/0/completed");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/1/completed");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/2/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/running");
        verify(jobNodeStorage).replaceJobNode(eq("execution/0/lastCompleteTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/1/lastCompleteTime"), anyLong());
        verify(jobNodeStorage).replaceJobNode(eq("execution/2/lastCompleteTime"), anyLong());
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNotMonitorExecution() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(false);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService, times(0)).isLeader();
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenIsNotLeader() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("leader/execution/cleaning")).thenReturn(true, false);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService).isLeader();
        verify(jobNodeStorage, times(2)).isJobNodeExisted("leader/execution/cleaning");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenIsLeaderButNotNeedFixExecutionInfo() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("leader/execution/necessary")).thenReturn(false);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService).isLeader();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/execution/cleaning", "");
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/completed");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/necessary");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/cleaning");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/cleaning");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForNewValuesGreater() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("leader/execution/necessary")).thenReturn(true);
        when(configService.getShardingTotalCount()).thenReturn(4);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService).isLeader();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/execution/cleaning", "");
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/completed");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/necessary");
        verify(configService).getShardingTotalCount();
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/3");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/necessary");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/cleaning");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/cleaning");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForNewValuesLess() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("leader/execution/necessary")).thenReturn(true);
        when(configService.getShardingTotalCount()).thenReturn(2);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService).isLeader();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/execution/cleaning", "");
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/completed");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/necessary");
        verify(configService).getShardingTotalCount();
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/necessary");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/cleaning");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/cleaning");
    }
    
    @Test
    public void assertCleanPreviousExecutionInfoWhenNeedFixExecutionInfoForNewValuesEqual() {
        when(jobNodeStorage.isJobNodeExisted("execution")).thenReturn(true);
        when(leaderElectionService.isLeader()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("leader/execution/necessary")).thenReturn(true);
        when(configService.getShardingTotalCount()).thenReturn(3);
        executionService.cleanPreviousExecutionInfo();
        verify(jobNodeStorage).isJobNodeExisted("execution");
        verify(leaderElectionService).isLeader();
        verify(jobNodeStorage).fillEphemeralJobNode("leader/execution/cleaning", "");
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/completed");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/completed");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/necessary");
        verify(configService).getShardingTotalCount();
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/necessary");
        verify(jobNodeStorage).removeJobNodeIfExisted("leader/execution/cleaning");
        verify(jobNodeStorage).isJobNodeExisted("leader/execution/cleaning");
    }
    
    @Test
    public void assertSetNeedFixExecutionInfoFlag() {
        executionService.setNeedFixExecutionInfoFlag();
        verify(jobNodeStorage).createJobNodeIfNeeded("leader/execution/necessary");
    }
    
    @Test
    public void assertClearRunningInfo() {
        executionService.clearRunningInfo(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/running");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/running");
    }
    
    @Test
    public void assertMisfireIfNotNecessaryWhenNotMonitorExecution() {
        when(configService.isMonitorExecution()).thenReturn(false);
        assertFalse(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
        verify(configService).isMonitorExecution();
    }
    
    @Test
    public void assertMisfireIfNotNecessary() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/2/running")).thenReturn(false);
        assertFalse(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/2/running");
    }
    
    @Test
    public void assertMisfireIfNecessary() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(true);
        assertTrue(executionService.misfireIfNecessary(Arrays.asList(0, 1, 2)));
        verify(configService, times(2)).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/0/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/1/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/2/misfire");
    }
    
    @Test
    public void assertSetMisfireWhenMonitorExecutionDisabled() {
        when(configService.isMonitorExecution()).thenReturn(false);
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("execution/0/misfire");
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("execution/1/misfire");
        verify(jobNodeStorage, times(0)).createJobNodeIfNeeded("execution/2/misfire");
    }
    
    @Test
    public void assertSetMisfire() {
        when(configService.isMonitorExecution()).thenReturn(true);
        executionService.setMisfire(Arrays.asList(0, 1, 2));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/0/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/1/misfire");
        verify(jobNodeStorage).createJobNodeIfNeeded("execution/2/misfire");
    }
    
    @Test
    public void assertGetMisfiredJobItems() {
        when(jobNodeStorage.isJobNodeExisted("execution/0/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/1/misfire")).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/2/misfire")).thenReturn(false);
        assertThat(executionService.getMisfiredJobItems(Arrays.asList(0, 1, 2)), is(Arrays.asList(0, 1)));
        verify(jobNodeStorage).isJobNodeExisted("execution/0/misfire");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/misfire");
        verify(jobNodeStorage).isJobNodeExisted("execution/2/misfire");
    }
    
    @Test
    public void assertClearMisfire() {
        executionService.clearMisfire(Arrays.asList(0, 1, 2));
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/0/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/1/misfire");
        verify(jobNodeStorage).removeJobNodeIfExisted("execution/2/misfire");
    }
    
    @Test
    public void assertRemoveExecutionInfo() {
        executionService.removeExecutionInfo();
        verify(jobNodeStorage).removeJobNodeIfExisted("execution");
    }
    
    @Test
    public void assertIsCompleted() {
        when(jobNodeStorage.isJobNodeExisted("execution/0/completed")).thenReturn(true);
        assertTrue(executionService.isCompleted(0));
    }
    
    @Test
    public void assertNotHaveRunningItemsWhenJNotMonitorExecution() {
        when(configService.isMonitorExecution()).thenReturn(false);
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage, times(0)).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage, times(0)).isJobNodeExisted("execution/1/running");
        verify(jobNodeStorage, times(0)).isJobNodeExisted("execution/2/running");
    }
    
    @Test
    public void assertHasRunningItems() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
    }
    
    @Test
    public void assertNotHaveRunningItems() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems(Arrays.asList(0, 1, 2)));
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/2/running");
    }
    
    @Test
    public void assertNotHaveRunningItemsWhenJNotMonitorExecutionForAll() {
        when(configService.isMonitorExecution()).thenReturn(false);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        assertFalse(executionService.hasRunningItems());
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
    }
    
    @Test
    public void assertHasRunningItemsForAll() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(true);
        assertTrue(executionService.hasRunningItems());
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
    }
    
    @Test
    public void assertNotHaveRunningItemsForAll() {
        when(configService.isMonitorExecution()).thenReturn(true);
        when(jobNodeStorage.getJobNodeChildrenKeys("execution")).thenReturn(Arrays.asList("0", "1", "2"));
        when(jobNodeStorage.isJobNodeExisted("execution/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/1/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("execution/2/running")).thenReturn(false);
        assertFalse(executionService.hasRunningItems());
        verify(configService).isMonitorExecution();
        verify(jobNodeStorage).getJobNodeChildrenKeys("execution");
        verify(jobNodeStorage).isJobNodeExisted("execution/0/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/1/running");
        verify(jobNodeStorage).isJobNodeExisted("execution/2/running");
    }
}
