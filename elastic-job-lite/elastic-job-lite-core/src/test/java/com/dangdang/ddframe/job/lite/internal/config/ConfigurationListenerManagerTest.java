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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.lite.internal.config.ConfigurationListenerManager.CronSettingChangedJobListener;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.lite.util.JobConfigurationUtil;
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
    
    private String simpleJobJson =  "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003da,1\\u003db\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"desc\","
            + "\"jobProperties\":{\"executor_service_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler\","
            + "\"job_exception_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler\"},"
            + "\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\",\"disabled\":true,\"overwrite\":true}";
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    private final ConfigurationListenerManager configurationListenerManager = new ConfigurationListenerManager(null, JobConfigurationUtil.createSimpleLiteJobConfiguration());
    
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
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config/other", null, simpleJobJson.getBytes())), "/test_job/config/other");
        verify(jobScheduleController, times(0)).rescheduleJob(Matchers.<String>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathButNotUpdate() {
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_ADDED, new ChildData("/test_job/config", null, simpleJobJson.getBytes())), "/test_job/config");
        verify(jobScheduleController, times(0)).rescheduleJob(Matchers.<String>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateButCannotFindJob() {
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/config", null, simpleJobJson.getBytes())), "/test_job/config");
        verify(jobScheduleController, times(0)).rescheduleJob(Matchers.<String>any());
    }
    
    @Test
    public void assertCronSettingChangedJobListenerWhenIsCronPathAndUpdateAndFindJob() {
        JobRegistry.getInstance().addJobScheduleController("test_job", jobScheduleController);
        configurationListenerManager.new CronSettingChangedJobListener().dataChanged(null, new TreeCacheEvent(
                TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/test_job/config", null, simpleJobJson.getBytes())), "/test_job/config");
        verify(jobScheduleController).rescheduleJob("0/1 * * * * ?");
    }
}
