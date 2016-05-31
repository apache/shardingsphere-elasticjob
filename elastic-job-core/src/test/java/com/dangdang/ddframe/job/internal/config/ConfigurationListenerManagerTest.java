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

package com.dangdang.ddframe.job.internal.config;

import com.dangdang.ddframe.job.api.config.SimpleJobConfiguration;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationListenerManager.CronSettingChangedJobListener;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class ConfigurationListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final ConfigurationListenerManager configurationListenerManager = new ConfigurationListenerManager(null, new SimpleJobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?"));
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(configurationListenerManager, configurationListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        configurationListenerManager.start();
        verify(jobNodeStorage).addDataListener(Matchers.<CronSettingChangedJobListener>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsNotCronPath() {
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/other", null, "*/10 * * * * *".getBytes())), "/testJob/config/other");
        verify(jobScheduleController, times(0)).rescheduleJob("*/10 * * * * *");
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathButNotUpdate() {
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/testJob/config/cron", null, "*/10 * * * * *".getBytes())), "/testJob/config/cron");
        verify(jobScheduleController, times(0)).rescheduleJob("*/10 * * * * *");
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateButCannotFindJob() {
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/cron", null, "*/10 * * * * *".getBytes())), "/testJob/config/cron");
        verify(jobScheduleController, times(0)).rescheduleJob("*/10 * * * * *");
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateAndFindJob() {
        JobRegistry.getInstance().addJobScheduleController("testJob", jobScheduleController);
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/testJob/config/cron", null, "*/10 * * * * *".getBytes())), "/testJob/config/cron");
        verify(jobScheduleController).rescheduleJob("*/10 * * * * *");
    }
}
