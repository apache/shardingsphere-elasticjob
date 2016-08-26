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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
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
import org.quartz.impl.triggers.CronTriggerImpl;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobScheduleControllerTest {
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    @Mock
    private Scheduler scheduler;
    
    @Mock
    private JobDetail jobDetail;
    
    private JobScheduleController jobScheduleController;
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        jobScheduleController = new JobScheduleController(scheduler, jobDetail, schedulerFacade, "test_job_Trigger");
    }
    
    @Test
    public void assertGetNextFireTimeWhenSchedulerExceptionOccur() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        JobKey jobKey = new JobKey("test_job");
        when(jobDetail.getKey()).thenReturn(jobKey);
        doThrow(SchedulerException.class).when(scheduler).getTriggersOfJob(jobKey);
        assertNull(jobScheduleController.getNextFireTime());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGetNextFireTime() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        JobKey jobKey = new JobKey("test_job");
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
        assertThat(jobScheduleController.getNextFireTime().getTime(), is(0L));
    }
    
    @Test
    public void assertTriggerJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(scheduler.isShutdown()).thenReturn(true);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        jobScheduleController.triggerJob();
        verify(jobDetail, times(0)).getKey();
        verify(scheduler, times(0)).triggerJob(jobKey);
    }
    
    @Test(expected = JobSystemException.class)
    public void assertTriggerJobFailure() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        when(jobDetail.getKey()).thenReturn(jobKey);
        doThrow(SchedulerException.class).when(scheduler).triggerJob(jobKey);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        try {
            jobScheduleController.triggerJob();
        } finally {
            verify(jobDetail).getKey();
            verify(scheduler).triggerJob(jobKey);
        }
    }
    
    @Test
    public void assertTriggerJobSuccess() throws NoSuchFieldException, SchedulerException {
        JobKey jobKey = new JobKey("test_job");
        when(jobDetail.getKey()).thenReturn(jobKey);
        when(scheduler.isShutdown()).thenReturn(false);
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        ReflectionUtils.setFieldValue(jobScheduleController, "jobDetail", jobDetail);
        jobScheduleController.triggerJob();
        verify(jobDetail).getKey();
        verify(scheduler).triggerJob(jobKey);
    }
    
    @Test
    public void assertShutdownJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.shutdown();
        verify(schedulerFacade).releaseJobResource();
        verify(scheduler, times(0)).shutdown();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertShutdownFailure() throws NoSuchFieldException, SchedulerException {
        doThrow(SchedulerException.class).when(scheduler).shutdown();
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.shutdown();
        } finally {
            verify(schedulerFacade).releaseJobResource();
            verify(scheduler).shutdown();
        }
    }
    
    @Test
    public void assertShutdownSuccess() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduleController.shutdown();
        verify(schedulerFacade).releaseJobResource();
        verify(scheduler).shutdown();
    }
    
    @Test
    public void assertRescheduleJobIfShutdown() throws NoSuchFieldException, SchedulerException {
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(true);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        verify(scheduler, times(0)).rescheduleJob(eq(TriggerKey.triggerKey("test_job_Trigger")), Matchers.<Trigger>any());
    }
    
    @Test(expected = JobSystemException.class)
    public void assertRescheduleJobFailure() throws NoSuchFieldException, SchedulerException {
        when(schedulerFacade.loadJobConfiguration()).thenReturn(
                LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(scheduler.getTrigger(TriggerKey.triggerKey("test_job_Trigger"))).thenReturn(new CronTriggerImpl());
        doThrow(SchedulerException.class).when(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("test_job_Trigger")), Matchers.<Trigger>any());
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        try {
            jobScheduleController.rescheduleJob("0/1 * * * * ?");
        } finally {
            verify(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("test_job_Trigger")), Matchers.<Trigger>any());
        }
    }
    
    @Test
    public void assertRescheduleJobSuccess() throws NoSuchFieldException, SchedulerException {
        when(schedulerFacade.loadJobConfiguration()).thenReturn(
                LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build());
        when(scheduler.getTrigger(TriggerKey.triggerKey("test_job_Trigger"))).thenReturn(new CronTriggerImpl());
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        verify(scheduler).rescheduleJob(eq(TriggerKey.triggerKey("test_job_Trigger")), Matchers.<Trigger>any());
    }
    
    @Test
    public void assertRescheduleJobWhenTriggerIsNull() throws NoSuchFieldException, SchedulerException {
        when(schedulerFacade.loadJobConfiguration()).thenReturn(
                LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestSimpleJob.class.getCanonicalName())).build());
        ReflectionUtils.setFieldValue(jobScheduleController, "scheduler", scheduler);
        when(scheduler.isShutdown()).thenReturn(false);
        jobScheduleController.rescheduleJob("0/1 * * * * ?");
        verify(scheduler, times(0)).rescheduleJob(eq(TriggerKey.triggerKey("test_job_Trigger")), Matchers.<Trigger>any());
    }
}
