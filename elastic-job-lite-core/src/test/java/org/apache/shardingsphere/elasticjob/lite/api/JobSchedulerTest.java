/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.api;

import org.apache.shardingsphere.elasticjob.lite.api.strategy.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.TestSimpleJob;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobTriggerListener;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class JobSchedulerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private SchedulerFacade schedulerFacade;
    
    private LiteJobConfiguration liteJobConfig;
    
    private JobScheduler jobScheduler;
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        liteJobConfig = LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "* * 0/10 * * ? 2050", 3).build(), TestSimpleJob.class.getCanonicalName())).build();
        jobScheduler = new JobScheduler(regCenter, new TestSimpleJob(), liteJobConfig);
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(jobScheduler, "regCenter", regCenter);
        ReflectionUtils.setFieldValue(jobScheduler, "schedulerFacade", schedulerFacade);
    }
    
    @Test
    public void assertInit() throws NoSuchFieldException, SchedulerException {
        when(schedulerFacade.updateJobConfiguration(liteJobConfig)).thenReturn(liteJobConfig);
        when(schedulerFacade.newJobTriggerListener()).thenReturn(new JobTriggerListener(null, null));
        jobScheduler.init();
        verify(schedulerFacade).registerStartUpInfo(true);
        Scheduler scheduler = ReflectionUtils.getFieldValue(JobRegistry.getInstance().getJobScheduleController("test_job"), JobScheduleController.class.getDeclaredField("scheduler"));
        assertThat(scheduler.getListenerManager().getTriggerListeners().get(0), instanceOf(JobTriggerListener.class));
        assertTrue(scheduler.isStarted());
        jobScheduler.shutdown();
    }

    @After
    public void tearDown() {
        JobRegistry.getInstance().shutdown("test_job");
    }
}
