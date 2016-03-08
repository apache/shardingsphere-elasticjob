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

package com.dangdang.ddframe.job.api;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobTriggerListener;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

public final class JobSchedulerTest {
    
    @Mock
    private CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    @Mock
    private ListenerManager listenerManager;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private StatisticsService statisticsService;
    
    @Mock
    private OffsetService offsetService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private Scheduler scheduler;
    
    @Mock
    private JobDetail jobDetail;
    
    private JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private JobScheduler jobScheduler = new JobScheduler(coordinatorRegistryCenter, jobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobScheduler, "coordinatorRegistryCenter", coordinatorRegistryCenter);
        ReflectionUtils.setFieldValue(jobScheduler, "listenerManager", listenerManager);
        ReflectionUtils.setFieldValue(jobScheduler, "configService", configService);
        ReflectionUtils.setFieldValue(jobScheduler, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(jobScheduler, "serverService", serverService);
        ReflectionUtils.setFieldValue(jobScheduler, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(jobScheduler, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(jobScheduler, "executionService", executionService);
        ReflectionUtils.setFieldValue(jobScheduler, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(jobScheduler, "statisticsService", statisticsService);
        ReflectionUtils.setFieldValue(jobScheduler, "offsetService", offsetService);
        ReflectionUtils.setFieldValue(jobScheduler, "monitorService", monitorService);
    }
    
    @Test
    public void assertScheduleWithElasticJobListeners() {
        JobScheduler jobScheduler = new JobScheduler(coordinatorRegistryCenter, jobConfig, new TestElasticJobListener(), new TestDistributeOnceElasticJobListener());
        List<ElasticJobListener> actual = ReflectionUtils.getFieldValue(jobScheduler, ReflectionUtils.getFieldWithName(JobScheduler.class, "elasticJobListeners", false));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), instanceOf(TestElasticJobListener.class));
        assertThat(actual.get(1), instanceOf(TestDistributeOnceElasticJobListener.class));
        Field field = ReflectionUtils.getFieldWithName(TestDistributeOnceElasticJobListener.class, "guaranteeService", false);
        field.setAccessible(true);
        assertNotNull(field);
    }
    
    @Test
    public void assertInitIfIsMisfire() throws NoSuchFieldException, SchedulerException {
        when(configService.getCron()).thenReturn("* * 0/10 * * ? 2050");
        when(configService.isMisfire()).thenReturn(true);
        jobScheduler.init();
        assertInit();
    }
    
    @Test
    public void assertInitIfIsNotMisfire() throws NoSuchFieldException, SchedulerException {
        when(configService.getCron()).thenReturn("* * 0/10 * * ? 2050");
        when(configService.isMisfire()).thenReturn(false);
        jobScheduler.init();
        assertInit();
    }
    
    private void assertInit() throws NoSuchFieldException, SchedulerException {
        JobDetail jobDetail = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("jobDetail"));
        assertThat(jobDetail.getKey().getName(), is("testJob"));
        assertThat((ConfigurationService) jobDetail.getJobDataMap().get("configService"), is(configService));
        assertThat((ShardingService) jobDetail.getJobDataMap().get("shardingService"), is(shardingService));
        assertThat((ExecutionContextService) jobDetail.getJobDataMap().get("executionContextService"), is(executionContextService));
        assertThat((ExecutionService) jobDetail.getJobDataMap().get("executionService"), is(executionService));
        assertThat((FailoverService) jobDetail.getJobDataMap().get("failoverService"), is(failoverService));
        assertThat((OffsetService) jobDetail.getJobDataMap().get("offsetService"), is(offsetService));
        Scheduler scheduler = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("scheduler"));
        assertThat(scheduler.getListenerManager().getTriggerListeners().size(), is(1));
        assertThat(scheduler.getListenerManager().getTriggerListeners().get(0), instanceOf(JobTriggerListener.class));
        assertTrue(scheduler.isStarted());
        verify(coordinatorRegistryCenter).addCacheData("/testJob");
        verify(listenerManager).startAllListeners();
        verify(leaderElectionService).leaderElection();
        verify(configService).persistJobConfiguration();
        verify(serverService).persistServerOnline();
        verify(serverService).clearJobStopedStatus();
        verify(statisticsService).startProcessCountJob();
        verify(shardingService).setReshardingFlag();
        verify(monitorService).listen();
    }
    
    @Test
    public void assertGetNextFireTimeWhenSchedulerExceptionOccur() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        JobKey jobKey = new JobKey("testJob");
        when(jobDetail.getKey()).thenReturn(jobKey);
        doThrow(SchedulerException.class).when(scheduler).getTriggersOfJob(jobKey);
        assertNull(jobScheduler.getNextFireTime());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGetNextFireTime() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        JobKey jobKey = new JobKey("testJob");
        Trigger trigger1 = mock(Trigger.class);
        Trigger trigger2 = mock(Trigger.class);
        Trigger trigger3 = mock(Trigger.class);
        Trigger trigger4 = mock(Trigger.class);
        @SuppressWarnings("rawtypes")
        List triggers = Arrays.asList(trigger1, trigger2, trigger3, trigger4);
        when(trigger1.getNextFireTime()).thenReturn(null);
        when(trigger2.getNextFireTime()).thenReturn(new Date(1L));
        when(trigger3.getNextFireTime()).thenReturn(new Date(100L));
        when(trigger4.getNextFireTime()).thenReturn(new Date(0L));
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(triggers);
        assertThat(jobScheduler.getNextFireTime().getTime(), is(0L));
    }
    
    @Test(expected = JobException.class)
    public void assertStopJobFailure() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        doThrow(SchedulerException.class).when(scheduler).pauseAll();
        try {
            jobScheduler.stopJob();
        } finally {
            verify(scheduler).pauseAll();
        }
    }
    
    @Test
    public void assertStopJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.stopJob();
        verify(scheduler).pauseAll();
    }
    
    @Test
    public void assertResumeManualStopedJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeManualStopedJob();
        verify(scheduler).isShutdown();
        verify(scheduler, times(0)).resumeAll();
    }
    
    @Test(expected = JobException.class)
    public void assertResumeManualStopedJobFailure() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        doThrow(SchedulerException.class).when(scheduler).resumeAll();
        try {
            jobScheduler.resumeManualStopedJob();
        } finally {
            verify(scheduler).isShutdown();
            verify(scheduler).resumeAll();
            verify(serverService, times(0)).clearJobStopedStatus();
        }
    }
    
    @Test
    public void assertResumeManualStopedJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeManualStopedJob();
        verify(scheduler).isShutdown();
        verify(scheduler).resumeAll();
        verify(serverService).clearJobStopedStatus();
    }
    
    @Test
    public void assertResumeCrashedJobIfIsJobStopedManually() throws NoSuchFieldException, SchedulerException {
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isJobStopedManually()).thenReturn(true);
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeCrashedJob();
        verify(serverService).persistServerOnline();
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
        verify(scheduler, times(0)).resumeAll();
    }
    
    @Test(expected = JobException.class)
    public void assertResumeCrashedJobFailure() throws NoSuchFieldException, SchedulerException {
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isJobStopedManually()).thenReturn(false);
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        doThrow(SchedulerException.class).when(scheduler).resumeAll();
        try {
            jobScheduler.resumeCrashedJob();
        } finally {
            verify(serverService).persistServerOnline();
            verify(shardingService).getLocalHostShardingItems();
            verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
            verify(scheduler).resumeAll();
        }
    }
    
    @Test
    public void assertResumeCrashedJobSuccess() throws NoSuchFieldException, SchedulerException {
        when(shardingService.getLocalHostShardingItems()).thenReturn(Arrays.asList(0, 1));
        when(serverService.isJobStopedManually()).thenReturn(false);
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeCrashedJob();
        verify(serverService).persistServerOnline();
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).clearRunningInfo(Arrays.asList(0, 1));
        verify(scheduler).resumeAll();
    }
    
    @Test(expected = JobException.class)
    public void assertTriggerJobFailure() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("testJob");
        when(jobDetail.getKey()).thenReturn(jobKey);
        doThrow(SchedulerException.class).when(scheduler).triggerJob(jobKey);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        try {
            jobScheduler.triggerJob();
        } finally {
            verify(jobDetail).getKey();
            verify(scheduler).triggerJob(jobKey);
        }
    }
    
    @Test
    public void assertTriggerJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("testJob");
        when(jobDetail.getKey()).thenReturn(jobKey);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        jobScheduler.triggerJob();
        verify(jobDetail).getKey();
        verify(scheduler).triggerJob(jobKey);
    }
    
    @Test(expected = JobException.class)
    public void assertShutdownFailure() throws NoSuchFieldException, SchedulerException {
        doThrow(SchedulerException.class).when(scheduler).shutdown();
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        try {
            jobScheduler.shutdown();
        } finally {
            verify(monitorService).close();
            verify(statisticsService).stopProcessCountJob();
            verify(scheduler).shutdown();
        }
    }
    
    @Test
    public void assertShutdownSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.shutdown();
        verify(monitorService).close();
        verify(statisticsService).stopProcessCountJob();
        verify(scheduler).shutdown();
    }
    
    @Test(expected = JobException.class)
    public void assertRescheduleJobFailure() throws NoSuchFieldException, SchedulerException {
        doThrow(SchedulerException.class).when(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("testJob_Trigger")), Matchers.<Trigger>any());
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        try {
            jobScheduler.rescheduleJob("0/1 * * * * ?");
        } finally {
            verify(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("testJob_Trigger")), Matchers.<Trigger>any());
        }
    }
    
    @Test
    public void assertRescheduleJobSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.rescheduleJob("0/1 * * * * ?");
        verify(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("testJob_Trigger")), Matchers.<Trigger>any());
    }
    
    @Test
    public void assertSetField() throws NoSuchFieldException, SchedulerException {
        jobScheduler.setField("fieldName", "fieldValue");
        JobDetail jobDetail = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("jobDetail"));
        assertThat(jobDetail.getJobDataMap().get("fieldName").toString(), is("fieldValue"));
    }
    
    static class TestElasticJobListener implements ElasticJobListener {
        
        @Override
        public void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        }
        
        @Override
        public void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
        }
    }
    
    static class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
        
        TestDistributeOnceElasticJobListener() {
            super(500000L, 500000L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext) {
        }
            
        @Override
        public void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext) {
        }
    }
}
