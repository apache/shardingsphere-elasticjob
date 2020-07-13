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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.producer;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TransientProducerSchedulerTest {
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private Scheduler scheduler;

    private TransientProducerScheduler transientProducerScheduler;
    
    private final CloudJobConfiguration jobConfig = CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job");
    
    private final JobDetail jobDetail = JobBuilder.newJob(TransientProducerScheduler.ProducerJob.class).withIdentity(jobConfig.getJobConfig().getCron()).build();
    
    private final Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobConfig.getJobConfig().getCron())
                        .withSchedule(CronScheduleBuilder.cronSchedule(jobConfig.getJobConfig().getCron())
                        .withMisfireHandlingInstructionDoNothing()).build();
    
    @Before
    public void setUp() throws NoSuchFieldException {
        transientProducerScheduler = new TransientProducerScheduler(readyService);
        ReflectionUtils.setFieldValue(transientProducerScheduler, "scheduler", scheduler);
    }
    
    @Test
    public void assertRegister() throws SchedulerException {
        when(scheduler.checkExists(jobDetail.getKey())).thenReturn(false);
        transientProducerScheduler.register(jobConfig);
        verify(scheduler).checkExists(jobDetail.getKey());
        verify(scheduler).scheduleJob(jobDetail, trigger);
    }
    
    @Test
    public void assertDeregister() throws SchedulerException {
        transientProducerScheduler.deregister(jobConfig);
        verify(scheduler).unscheduleJob(TriggerKey.triggerKey(jobConfig.getJobConfig().getCron()));
    }
    
    @Test
    public void assertShutdown() throws SchedulerException {
        transientProducerScheduler.shutdown();
        verify(scheduler).isShutdown();
        verify(scheduler).shutdown();
    }
}
