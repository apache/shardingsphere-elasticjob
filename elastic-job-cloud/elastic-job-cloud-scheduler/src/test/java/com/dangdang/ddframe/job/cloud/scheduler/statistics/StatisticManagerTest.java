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

package com.dangdang.ddframe.job.cloud.scheduler.statistics;

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.job.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.job.JobRunningStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskResultStatistics;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningStatistics;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatisticManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private Optional<JobEventRdbConfiguration> jobEventRdbConfiguration;
    
    @Mock
    private StatisticRdbRepository rdbRepository;
    
    @Mock
    private StatisticsScheduler scheduler;
    
    @Mock
    private CloudJobConfigurationService configurationService;
    
    private StatisticManager statisticManager;
    
    @Before
    public void setUp() {
        statisticManager = StatisticManager.getInstance(regCenter, jobEventRdbConfiguration);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        statisticManager.shutdown();
        ReflectionUtils.setFieldValue(StatisticManager.class, StatisticManager.class.getDeclaredField("instance"), null);
        reset(configurationService);
        reset(rdbRepository);
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(statisticManager, is(StatisticManager.getInstance(regCenter, jobEventRdbConfiguration)));
    }
    
    @Test
    public void assertStartupWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        statisticManager.startup();
    }
    
    @Test
    public void assertStartupWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        statisticManager.startup();
    }
    
    @Test
    public void assertShutdown() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "scheduler", scheduler);
        statisticManager.shutdown();
        verify(scheduler).shutdown();
    }
    
    @Test
    public void assertTaskRun() throws NoSuchFieldException {
        statisticManager.taskRunSuccessfully();
        statisticManager.taskRunFailed();
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getFailedCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getFailedCount(), is(0));
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.getSummedTaskResultStatistics(any(Date.class), any(StatisticInterval.class)))
            .thenReturn(new TaskResultStatistics(10, 10, StatisticInterval.DAY, new Date()));
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getSuccessCount(), is(10));
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getFailedCount(), is(10));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getSuccessCount(), is(10));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getFailedCount(), is(10));
        verify(rdbRepository, times(4)).getSummedTaskResultStatistics(any(Date.class), any(StatisticInterval.class));
    }
    
    @Test
    public void assertJobTypeStatistics() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "configurationService", configurationService);
        when(configurationService.loadAll()).thenReturn(Lists.newArrayList(
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_simple"), 
                CloudJobConfigurationBuilder.createDataflowCloudJobConfiguration("test_job_dataflow"), 
                CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("test_job_script")));
        assertThat(statisticManager.getJobTypeStatistics().getSimpleJobCount(), is(1));
        assertThat(statisticManager.getJobTypeStatistics().getDataflowJobCount(), is(1));
        assertThat(statisticManager.getJobTypeStatistics().getScriptJobCount(), is(1));
        verify(configurationService, times(3)).loadAll();
    }
    
    @Test
    public void assertJobExecutionTypeStatistics() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "configurationService", configurationService);
        when(configurationService.loadAll()).thenReturn(Lists.newArrayList(
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1", CloudJobExecutionType.DAEMON), 
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_2", CloudJobExecutionType.TRANSIENT)));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getDaemonJobCount(), is(1));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getTransientJobCount(), is(1));
        verify(configurationService, times(2)).loadAll();
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findTaskRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findTaskRunningStatistics(any(Date.class)))
            .thenReturn(Lists.newArrayList(new TaskRunningStatistics(10, new Date())));
        assertThat(statisticManager.findTaskRunningStatisticsWeekly().size(), is(1));
        verify(rdbRepository).findTaskRunningStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findJobRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findJobRunningStatistics(any(Date.class)))
            .thenReturn(Lists.newArrayList(new JobRunningStatistics(10, new Date())));
        assertThat(statisticManager.findJobRunningStatisticsWeekly().size(), is(1));
        verify(rdbRepository).findJobRunningStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findJobRegisterStatisticsSinceOnline().isEmpty());
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findJobRegisterStatistics(any(Date.class)))
            .thenReturn(Lists.newArrayList(new JobRegisterStatistics(10, new Date())));
        assertThat(statisticManager.findJobRegisterStatisticsSinceOnline().size(), is(1));
        verify(rdbRepository).findJobRegisterStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics actual = statisticManager.findLatestTaskResultStatistics(each);
            assertThat(actual.getSuccessCount(), is(0));
            assertThat(actual.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        for (StatisticInterval each : StatisticInterval.values()) {
            when(rdbRepository.findLatestTaskResultStatistics(each))
                .thenReturn(Optional.of(new TaskResultStatistics(10, 5, each, new Date())));
            TaskResultStatistics actual = statisticManager.findLatestTaskResultStatistics(each);
            assertThat(actual.getSuccessCount(), is(10));
            assertThat(actual.getFailedCount(), is(5));
        }
        verify(rdbRepository, times(StatisticInterval.values().length)).findLatestTaskResultStatistics(any(StatisticInterval.class));
    }
    
    @Test
    public void assertFindTaskResultStatisticsDailyWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findTaskResultStatisticsDaily().isEmpty());
    }
    
    @Test
    public void assertFindTaskResultStatisticsDailyWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findTaskResultStatistics(any(Date.class), any(StatisticInterval.class)))
            .thenReturn(Lists.newArrayList(new TaskResultStatistics(10, 5, StatisticInterval.MINUTE, new Date())));
        assertThat(statisticManager.findTaskResultStatisticsDaily().size(), is(1));
        verify(rdbRepository).findTaskResultStatistics(any(Date.class), any(StatisticInterval.class));
    }
}
