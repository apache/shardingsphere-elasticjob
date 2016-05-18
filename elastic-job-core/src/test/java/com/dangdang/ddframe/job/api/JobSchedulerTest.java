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
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.internal.schedule.JobTriggerListener;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        Scheduler scheduler = ReflectionUtils.getFieldValue(JobRegistry.getInstance().getJobScheduleController("testJob"), JobScheduleController.class.getDeclaredField("scheduler"));
        assertThat(scheduler.getListenerManager().getTriggerListeners().size(), is(1));
        assertThat(scheduler.getListenerManager().getTriggerListeners().get(0), instanceOf(JobTriggerListener.class));
        assertTrue(scheduler.isStarted());
        assertThat((JobFacade) jobDetail.getJobDataMap().get("jobFacade"), is(jobFacade));
        verify(regCenter).addCacheData("/testJob");
        verify(schedulerFacade).registerStartUpInfo();
        verify(schedulerFacade).newJobTriggerListener();
    }
}
