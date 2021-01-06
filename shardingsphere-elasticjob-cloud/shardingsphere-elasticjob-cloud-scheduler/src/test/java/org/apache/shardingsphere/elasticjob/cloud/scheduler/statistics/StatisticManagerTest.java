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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics;

import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class StatisticManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private StatisticRdbRepository rdbRepository;
    
    @Mock
    private StatisticsScheduler scheduler;
    
    @Mock
    private CloudJobConfigurationService configurationService;
    
    private StatisticManager statisticManager;
    
    @Before
    public void setUp() {
        statisticManager = StatisticManager.getInstance(regCenter, null);
    }
    
    @After
    public void tearDown() {
        statisticManager.shutdown();
        ReflectionUtils.setStaticFieldValue(StatisticManager.class, "instance", null);
        reset(configurationService);
        reset(rdbRepository);
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(statisticManager, is(StatisticManager.getInstance(regCenter, null)));
    }
    
    @Test
    public void assertStartupWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        statisticManager.startup();
    }
    
    @Test
    public void assertStartupWhenRdbIsConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        statisticManager.startup();
    }
    
    @Test
    public void assertShutdown() {
        ReflectionUtils.setFieldValue(statisticManager, "scheduler", scheduler);
        statisticManager.shutdown();
        verify(scheduler).shutdown();
    }
    
    @Test
    public void assertTaskRun() {
        statisticManager.taskRunSuccessfully();
        statisticManager.taskRunFailed();
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsWeekly().getFailedCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getSuccessCount(), is(0));
        assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getFailedCount(), is(0));
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsConfigured() {
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
    public void assertJobExecutionTypeStatistics() {
        ReflectionUtils.setFieldValue(statisticManager, "configurationService", configurationService);
        when(configurationService.loadAll()).thenReturn(Arrays.asList(
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1", CloudJobExecutionType.DAEMON), 
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_2", CloudJobExecutionType.TRANSIENT)));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getDaemonJobCount(), is(1));
        assertThat(statisticManager.getJobExecutionTypeStatistics().getTransientJobCount(), is(1));
        verify(configurationService, times(2)).loadAll();
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findTaskRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findTaskRunningStatistics(any(Date.class))).thenReturn(Collections.singletonList(new TaskRunningStatistics(10, new Date())));
        assertThat(statisticManager.findTaskRunningStatisticsWeekly().size(), is(1));
        verify(rdbRepository).findTaskRunningStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findJobRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findJobRunningStatistics(any(Date.class))).thenReturn(Collections.singletonList(new JobRunningStatistics(10, new Date())));
        assertThat(statisticManager.findJobRunningStatisticsWeekly().size(), is(1));
        verify(rdbRepository).findJobRunningStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findJobRegisterStatisticsSinceOnline().isEmpty());
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findJobRegisterStatistics(any(Date.class))).thenReturn(Collections.singletonList(new JobRegisterStatistics(10, new Date())));
        assertThat(statisticManager.findJobRegisterStatisticsSinceOnline().size(), is(1));
        verify(rdbRepository).findJobRegisterStatistics(any(Date.class));
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics actual = statisticManager.findLatestTaskResultStatistics(each);
            assertThat(actual.getSuccessCount(), is(0));
            assertThat(actual.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsConfigured() {
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
    public void assertFindTaskResultStatisticsDailyWhenRdbIsNotConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        assertTrue(statisticManager.findTaskResultStatisticsDaily().isEmpty());
    }
    
    @Test
    public void assertFindTaskResultStatisticsDailyWhenRdbIsConfigured() {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        when(rdbRepository.findTaskResultStatistics(any(Date.class), any(StatisticInterval.class)))
            .thenReturn(Collections.singletonList(new TaskResultStatistics(10, 5, StatisticInterval.MINUTE, new Date())));
        assertThat(statisticManager.findTaskResultStatisticsDaily().size(), is(1));
        verify(rdbRepository).findTaskResultStatistics(any(Date.class), any(StatisticInterval.class));
    }
}
