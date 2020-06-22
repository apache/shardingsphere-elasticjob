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

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteJsonConstants;
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class RescheduleListenerManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final RescheduleListenerManager rescheduleListenerManager = new RescheduleListenerManager(null, "test_job");
    
    @Before
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(rescheduleListenerManager, rescheduleListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        rescheduleListenerManager.start();
        verify(jobNodeStorage).addDataListener(ArgumentMatchers.<RescheduleListenerManager.CronSettingAndJobEventChangedJobListener>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsNotCronPath() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().dataChanged("/test_job/config/other", Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(jobScheduleController, times(0)).rescheduleJob(ArgumentMatchers.any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathButNotUpdate() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().dataChanged("/test_job/config", Type.NODE_ADDED, LiteJsonConstants.getJobJson());
        verify(jobScheduleController, times(0)).rescheduleJob(ArgumentMatchers.any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateButCannotFindJob() {
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().dataChanged("/test_job/config", Type.NODE_UPDATED, LiteJsonConstants.getJobJson());
        verify(jobScheduleController, times(0)).rescheduleJob(ArgumentMatchers.any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateAndFindJob() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        rescheduleListenerManager.new CronSettingAndJobEventChangedJobListener().dataChanged("/test_job/config", Type.NODE_UPDATED, LiteJsonConstants.getJobJson());
        verify(jobScheduleController).rescheduleJob("0/1 * * * * ?");
        JobRegistry.getInstance().shutdown("test_job");
    }
}
