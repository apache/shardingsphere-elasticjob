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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics.StatisticUnit;
import com.dangdang.ddframe.job.statistics.type.TaskRunningStatistics;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class StatisticManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private Optional<DataSource> dataSource;
    
    @Mock
    private StatisticRdbRepository rdbRepository;
    
    @Mock
    private StatisticsScheduler scheduler;
    
    @Mock
    private ConfigurationService configurationService;
    
    private StatisticManager statisticManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(StatisticManager.class, StatisticManager.class.getDeclaredField("instance"), null);
        statisticManager = StatisticManager.getInstance(regCenter, dataSource);
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(statisticManager, is(StatisticManager.getInstance(regCenter, dataSource)));
    }
    
    @Test
    public void assertStartupWhenRDBRepositoryIsNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        statisticManager.startup();
    }
    
    @Test
    public void assertStartupWhenRDBRepositoryIsNotNull() throws NoSuchFieldException {
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
    public void assertTaskRunningResultStatisticsWhenRDBRepositoryIsNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertThat(statisticManager.getTaskRunningResultStatisticsWeekly().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskRunningResultStatisticsWeekly().getFailedCount(), is(0));
        assertThat(statisticManager.getTaskRunningResultStatisticsSinceOnline().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskRunningResultStatisticsSinceOnline().getFailedCount(), is(0));
    }
    
    @Test
    public void assertTaskRunningResultStatisticsWhenRDBRepositoryIsNotNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.getSummedTaskRunningResultStatistics(any(Date.class), any(StatisticUnit.class)))
            .thenReturn(new TaskRunningResultStatistics(10, 10, StatisticUnit.DAY, new Date()));
        assertThat(statisticManager.getTaskRunningResultStatisticsWeekly().getSuccessCount(), is(10));
        assertThat(statisticManager.getTaskRunningResultStatisticsWeekly().getFailedCount(), is(10));
        assertThat(statisticManager.getTaskRunningResultStatisticsSinceOnline().getSuccessCount(), is(10));
        assertThat(statisticManager.getTaskRunningResultStatisticsSinceOnline().getFailedCount(), is(10));
        verify(rdbRepository, times(4)).getSummedTaskRunningResultStatistics(any(Date.class), any(StatisticUnit.class));
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
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1", JobExecutionType.DAEMON), 
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_2", JobExecutionType.TRANSIENT)));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getDaemonJobCount(), is(1));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getTransientJobCount(), is(1));
        verify(configurationService, times(2)).loadAll();
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRDBRepositoryIsNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findTaskRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRDBRepositoryIsNotNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findTaskRunningStatistics(any(Date.class)))
            .thenReturn(Lists.newArrayList(new TaskRunningStatistics(10, new Date())));
        assertThat(statisticManager.findTaskRunningStatisticsWeekly().size(), is(1));
        verify(rdbRepository).findTaskRunningStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRDBRepositoryIsNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findJobRegisterStatisticsSinceOnline().isEmpty());
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRDBRepositoryIsNotNull() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findJobRegisterStatistics(any(Date.class)))
            .thenReturn(Lists.newArrayList(new JobRegisterStatistics(10, new Date())));
        assertThat(statisticManager.findJobRegisterStatisticsSinceOnline().size(), is(1));
        verify(rdbRepository).findJobRegisterStatistics(any(Date.class));
    }
}
