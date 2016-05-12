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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.api.listener.fixture.TestDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.fixture.TestElasticJobListener;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobTriggerListener;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

public final class JobSchedulerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private Scheduler scheduler;
    
    @Mock
    private JobDetail jobDetail;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private JobScheduler jobScheduler = new JobScheduler(regCenter, jobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobScheduler, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(jobScheduler, "schedulerFacade", schedulerFacade);
        ReflectionUtils.setFieldValue(jobScheduler, "jobFacade", jobFacade);
    }
    
    @Test
    public void testNew() throws NoSuchFieldException {
        JobScheduler actualJobScheduler = new JobScheduler(null, jobConfig, new TestElasticJobListener(caller), new TestDistributeOnceElasticJobListener(caller));
        JobFacade actualJobFacade = ReflectionUtils.getFieldValue(actualJobScheduler, ReflectionUtils.getFieldWithName(JobScheduler.class, "jobFacade", false));
        List<ElasticJobListener> actualElasticJobListeners = ReflectionUtils.getFieldValue(actualJobFacade, ReflectionUtils.getFieldWithName(JobFacade.class, "elasticJobListeners", false));
        assertThat(actualElasticJobListeners.size(), Is.is(2));
        assertThat(actualElasticJobListeners.get(0), IsInstanceOf.instanceOf(TestElasticJobListener.class));
        assertThat(actualElasticJobListeners.get(1), IsInstanceOf.instanceOf(TestDistributeOnceElasticJobListener.class));
        assertNotNull(ReflectionUtils.getFieldValue(actualElasticJobListeners.get(1), AbstractDistributeOnceElasticJobListener.class.getDeclaredField("guaranteeService")));
    }
    
    @Test
    public void assertInitIfIsMisfire() throws NoSuchFieldException, SchedulerException {
        mockInit(true);
        jobScheduler.init();
        assertInit();
    }
    
    @Test
    public void assertInitIfIsNotMisfire() throws NoSuchFieldException, SchedulerException {
        mockInit(false);
        jobScheduler.init();
        assertInit();
    }
    
    private void mockInit(final boolean isMisfire) {
        when(schedulerFacade.newJobTriggerListener()).thenReturn(new JobTriggerListener(null, null));
        when(schedulerFacade.getCron()).thenReturn("* * 0/10 * * ? 2050");
        when(schedulerFacade.isMisfire()).thenReturn(isMisfire);
    }
    
    private void assertInit() throws NoSuchFieldException, SchedulerException {
        verify(schedulerFacade).clearPreviousServerStatus();
        JobDetail jobDetail = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("jobDetail"));
        assertThat(jobDetail.getKey().getName(), is("testJob"));
        Scheduler scheduler = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("scheduler"));
        assertThat(scheduler.getListenerManager().getTriggerListeners().size(), is(1));
        assertThat(scheduler.getListenerManager().getTriggerListeners().get(0), instanceOf(JobTriggerListener.class));
        assertTrue(scheduler.isStarted());
        assertThat((JobFacade) jobDetail.getJobDataMap().get("jobFacade"), is(jobFacade));
        verify(regCenter).addCacheData("/testJob");
        verify(schedulerFacade).registerStartUpInfo();
        verify(schedulerFacade).newJobTriggerListener();
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
    
    @Test
    public void assertPauseJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.pauseJob();
        verify(scheduler).isShutdown();
        verify(scheduler, times(0)).pauseAll();
    }
    
    @Test(expected = JobException.class)
    public void assertPauseJobFailure() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        doThrow(SchedulerException.class).when(scheduler).pauseAll();
        try {
            jobScheduler.pauseJob();
        } finally {
            verify(scheduler).pauseAll();
        }
    }
    
    @Test
    public void assertPauseJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduler.pauseJob();
        verify(scheduler).pauseAll();
    }
    
    @Test
    public void assertResumeJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeJob();
        verify(scheduler).isShutdown();
        verify(scheduler, times(0)).resumeAll();
    }
    
    @Test(expected = JobException.class)
    public void assertResumeJobFailure() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        doThrow(SchedulerException.class).when(scheduler).resumeAll();
        try {
            jobScheduler.resumeJob();
        } finally {
            verify(scheduler).isShutdown();
            verify(scheduler).resumeAll();
        }
    }
    
    @Test
    public void assertResumeJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobRegistry.getInstance().addJobInstance("testJob", new TestJob());
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        jobScheduler.resumeJob();
        verify(scheduler).isShutdown();
        verify(scheduler).resumeAll();
    }
    
    @Test
    public void assertTriggerJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("testJob");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        jobScheduler.triggerJob();
        verify(jobDetail, times(0)).getKey();
        verify(scheduler, times(0)).triggerJob(jobKey);
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
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduler, "jobDetail", jobDetail);
        jobScheduler.triggerJob();
        verify(jobDetail).getKey();
        verify(scheduler).triggerJob(jobKey);
    }
    
    @Test
    public void assertShutdownJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduler.shutdown();
        verify(schedulerFacade).releaseJobResource();
        verify(scheduler, times(0)).shutdown();
    }
    
    @Test(expected = JobException.class)
    public void assertShutdownFailure() throws NoSuchFieldException, SchedulerException {
        doThrow(SchedulerException.class).when(scheduler).shutdown();
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        try {
            jobScheduler.shutdown();
        } finally {
            verify(schedulerFacade).releaseJobResource();
            verify(scheduler).shutdown();
        }
    }
    
    @Test
    public void assertShutdownSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduler.shutdown();
        verify(schedulerFacade).releaseJobResource();
        verify(scheduler).shutdown();
    }
    
    @Test
    public void assertRescheduleJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduler, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduler.rescheduleJob("0/1 * * * * ?");
        verify(scheduler, times(0)).rescheduleJob(eq(TriggerKey.triggerKey("testJob_Trigger")), Matchers.<Trigger>any());
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
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduler.rescheduleJob("0/1 * * * * ?");
        verify(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("testJob_Trigger")), Matchers.<Trigger>any());
    }
    
    @Test
    public void assertSetField() throws NoSuchFieldException, SchedulerException {
        jobScheduler.setField("fieldName", "fieldValue");
        JobDetail jobDetail = ReflectionUtils.getFieldValue(jobScheduler, jobScheduler.getClass().getDeclaredField("jobDetail"));
        assertThat(jobDetail.getJobDataMap().get("fieldName").toString(), is("fieldValue"));
    }
}
