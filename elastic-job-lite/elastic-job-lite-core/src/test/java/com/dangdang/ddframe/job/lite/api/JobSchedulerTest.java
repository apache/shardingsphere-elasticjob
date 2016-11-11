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

package com.dangdang.ddframe.job.lite.api;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.fixture.ElasticJobListenerCaller;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.fixture.util.JobConfigurationUtil;
import com.dangdang.ddframe.job.lite.internal.executor.JobExecutor;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.schedule.JobTriggerListener;
import com.dangdang.ddframe.job.lite.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobSchedulerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobExecutor jobExecutor;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    @Mock
    private ElasticJobListenerCaller caller;
    
    private final LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration();
    
    private JobScheduler jobScheduler = new JobScheduler(regCenter, liteJobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobScheduler, "jobExecutor", jobExecutor);
        when(jobExecutor.getSchedulerFacade()).thenReturn(schedulerFacade);
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
        when(schedulerFacade.loadJobConfiguration()).thenReturn(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "* * 0/10 * * ? 2050", 3).misfire(isMisfire).build(), TestSimpleJob.class.getCanonicalName())).build());
    }
    
    private void assertInit() throws NoSuchFieldException, SchedulerException {
        verify(jobExecutor).init();
        Scheduler scheduler = ReflectionUtils.getFieldValue(JobRegistry.getInstance().getJobScheduleController("test_job"), JobScheduleController.class.getDeclaredField("scheduler"));
        assertThat(scheduler.getListenerManager().getTriggerListeners().size(), is(1));
        assertThat(scheduler.getListenerManager().getTriggerListeners().get(0), instanceOf(JobTriggerListener.class));
        assertTrue(scheduler.isStarted());
        verify(schedulerFacade).newJobTriggerListener();
    }
    
    @Test
    public void assertShutdown() {
        mockInit(true);
        jobScheduler.init();
        jobScheduler.shutdown();
    }
}
