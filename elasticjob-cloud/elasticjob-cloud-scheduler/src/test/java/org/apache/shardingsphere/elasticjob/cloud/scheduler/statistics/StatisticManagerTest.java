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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.cloud.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class StatisticManagerTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private Optional<TracingConfiguration> tracingConfiguration;
    
    @Mock
    private StatisticRdbRepository rdbRepository;
    
    @Mock
    private StatisticsScheduler scheduler;
    
    @Mock
    private CloudJobConfigurationService configurationService;
    
    private StatisticManager statisticManager;
    
    @Before
    public void setUp() {
        statisticManager = StatisticManager.getInstance(regCenter, tracingConfiguration);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        statisticManager.shutdown();
        ReflectionUtils.setFieldValue(StatisticManager.class, StatisticManager.class.getDeclaredField("instance"), null);
        Mockito.reset(configurationService);
        Mockito.reset(rdbRepository);
    }
    
    @Test
    public void assertGetInstance() {
        Assert.assertThat(statisticManager, Is.is(StatisticManager.getInstance(regCenter, tracingConfiguration)));
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
        Mockito.verify(scheduler).shutdown();
    }
    
    @Test
    public void assertTaskRun() throws NoSuchFieldException {
        statisticManager.taskRunSuccessfully();
        statisticManager.taskRunFailed();
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        Assert.assertThat(statisticManager.getTaskResultStatisticsWeekly().getSuccessCount(), Is.is(0));
        Assert.assertThat(statisticManager.getTaskResultStatisticsWeekly().getFailedCount(), Is.is(0));
        Assert.assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getSuccessCount(), Is.is(0));
        Assert.assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getFailedCount(), Is.is(0));
    }
    
    @Test
    public void assertTaskResultStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        Mockito.when(rdbRepository.getSummedTaskResultStatistics(Mockito.any(Date.class), Mockito.any(StatisticInterval.class)))
            .thenReturn(new TaskResultStatistics(10, 10, StatisticInterval.DAY, new Date()));
        Assert.assertThat(statisticManager.getTaskResultStatisticsWeekly().getSuccessCount(), Is.is(10));
        Assert.assertThat(statisticManager.getTaskResultStatisticsWeekly().getFailedCount(), Is.is(10));
        Assert.assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getSuccessCount(), Is.is(10));
        Assert.assertThat(statisticManager.getTaskResultStatisticsSinceOnline().getFailedCount(), Is.is(10));
        Mockito.verify(rdbRepository, Mockito.times(4)).getSummedTaskResultStatistics(Mockito.any(Date.class), Mockito.any(StatisticInterval.class));
    }
    
    @Test
    public void assertJobTypeStatistics() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "configurationService", configurationService);
        Mockito.when(configurationService.loadAll()).thenReturn(Lists.newArrayList(
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_simple"), 
                CloudJobConfigurationBuilder.createDataflowCloudJobConfiguration("test_job_dataflow"), 
                CloudJobConfigurationBuilder.createScriptCloudJobConfiguration("test_job_script")));
        Assert.assertThat(statisticManager.getJobTypeStatistics().getSimpleJobCount(), Is.is(1));
        Assert.assertThat(statisticManager.getJobTypeStatistics().getDataflowJobCount(), Is.is(1));
        Assert.assertThat(statisticManager.getJobTypeStatistics().getScriptJobCount(), Is.is(1));
        Mockito.verify(configurationService, Mockito.times(3)).loadAll();
    }
    
    @Test
    public void assertJobExecutionTypeStatistics() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "configurationService", configurationService);
        Mockito.when(configurationService.loadAll()).thenReturn(Lists.newArrayList(
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1", CloudJobExecutionType.DAEMON),
                CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_2", CloudJobExecutionType.TRANSIENT)));
        Assert.assertThat(statisticManager.getJobExecutionTypeStatistics().getDaemonJobCount(), Is.is(1));
        Assert.assertThat(statisticManager.getJobExecutionTypeStatistics().getTransientJobCount(), Is.is(1));
        Mockito.verify(configurationService, Mockito.times(2)).loadAll();
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        Assert.assertTrue(statisticManager.findTaskRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        Mockito.when(rdbRepository.findTaskRunningStatistics(Mockito.any(Date.class)))
            .thenReturn(Lists.newArrayList(new TaskRunningStatistics(10, new Date())));
        Assert.assertThat(statisticManager.findTaskRunningStatisticsWeekly().size(), Is.is(1));
        Mockito.verify(rdbRepository).findTaskRunningStatistics(Mockito.any(Date.class));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        Assert.assertTrue(statisticManager.findJobRunningStatisticsWeekly().isEmpty());
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        Mockito.when(rdbRepository.findJobRunningStatistics(Mockito.any(Date.class)))
            .thenReturn(Lists.newArrayList(new JobRunningStatistics(10, new Date())));
        Assert.assertThat(statisticManager.findJobRunningStatisticsWeekly().size(), Is.is(1));
        Mockito.verify(rdbRepository).findJobRunningStatistics(Mockito.any(Date.class));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        Assert.assertTrue(statisticManager.findJobRegisterStatisticsSinceOnline().isEmpty());
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        Mockito.when(rdbRepository.findJobRegisterStatistics(Mockito.any(Date.class)))
            .thenReturn(Lists.newArrayList(new JobRegisterStatistics(10, new Date())));
        Assert.assertThat(statisticManager.findJobRegisterStatisticsSinceOnline().size(), Is.is(1));
        Mockito.verify(rdbRepository).findJobRegisterStatistics(Mockito.any(Date.class));
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics actual = statisticManager.findLatestTaskResultStatistics(each);
            Assert.assertThat(actual.getSuccessCount(), Is.is(0));
            Assert.assertThat(actual.getFailedCount(), Is.is(0));
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatisticsWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        for (StatisticInterval each : StatisticInterval.values()) {
            Mockito.when(rdbRepository.findLatestTaskResultStatistics(each))
                .thenReturn(Optional.of(new TaskResultStatistics(10, 5, each, new Date())));
            TaskResultStatistics actual = statisticManager.findLatestTaskResultStatistics(each);
            Assert.assertThat(actual.getSuccessCount(), Is.is(10));
            Assert.assertThat(actual.getFailedCount(), Is.is(5));
        }
        Mockito.verify(rdbRepository, Mockito.times(StatisticInterval.values().length)).findLatestTaskResultStatistics(Mockito.any(StatisticInterval.class));
    }
    
    @Test
    public void assertFindTaskResultStatisticsDailyWhenRdbIsNotConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", null);
        Assert.assertTrue(statisticManager.findTaskResultStatisticsDaily().isEmpty());
    }
    
    @Test
    public void assertFindTaskResultStatisticsDailyWhenRdbIsConfigured() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(statisticManager, "rdbRepository", rdbRepository);
        Mockito.when(rdbRepository.findTaskResultStatistics(Mockito.any(Date.class), Mockito.any(StatisticInterval.class)))
            .thenReturn(Lists.newArrayList(new TaskResultStatistics(10, 5, StatisticInterval.MINUTE, new Date())));
        Assert.assertThat(statisticManager.findTaskResultStatisticsDaily().size(), Is.is(1));
        Mockito.verify(rdbRepository).findTaskResultStatistics(Mockito.any(Date.class), Mockito.any(StatisticInterval.class));
    }
}
