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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.job;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.TaskRunningResultMetaData;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.task.TaskRunningResultStatistics;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class TaskRunningResultStatisticJobTest {
    
    private StatisticInterval statisticInterval = StatisticInterval.MINUTE;
    
    private TaskRunningResultMetaData sharedData;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private TaskRunningResultStatisticJob taskRunningResultStatisticJob;
    
    @Before
    public void setUp() {
        taskRunningResultStatisticJob = new TaskRunningResultStatisticJob();
        sharedData = new TaskRunningResultMetaData();
        taskRunningResultStatisticJob.setStatisticInterval(statisticInterval);
        taskRunningResultStatisticJob.setSharedData(sharedData);
        taskRunningResultStatisticJob.setRepository(repository);
    }
    
    @Test
    public void assertBuildJobDetail() {
        assertThat(taskRunningResultStatisticJob.buildJobDetail().getKey().getName(), is(TaskRunningResultStatisticJob.class.getSimpleName() + "_" + statisticInterval));
    }
    
    @Test
    public void assertBuildTrigger() throws SchedulerException {
        for (StatisticInterval each : StatisticInterval.values()) {
            taskRunningResultStatisticJob.setStatisticInterval(each);
            Trigger trigger = taskRunningResultStatisticJob.buildTrigger();
            assertThat(trigger.getKey().getName(), is(TaskRunningResultStatisticJob.class.getSimpleName() + "Trigger" + "_" + each));
        }
    }
    
    @Test
    public void assertGetDataMap() throws SchedulerException {
        assertThat((StatisticInterval) taskRunningResultStatisticJob.getDataMap().get("statisticInterval"), is(statisticInterval));
        assertThat((TaskRunningResultMetaData) taskRunningResultStatisticJob.getDataMap().get("sharedData"), is(sharedData));
        assertThat((StatisticRdbRepository) taskRunningResultStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() throws SchedulerException {
        Optional<TaskRunningResultStatistics> latestOne = Optional.absent();
        for (StatisticInterval each : StatisticInterval.values()) {
            taskRunningResultStatisticJob.setStatisticInterval(each);
            when(repository.findLatestTaskRunningResultStatistics(each)).thenReturn(latestOne);
            when(repository.add(any(TaskRunningResultStatistics.class))).thenReturn(true);
            taskRunningResultStatisticJob.execute(null);
            verify(repository).findLatestTaskRunningResultStatistics(each);
        }
        verify(repository, times(3)).add(any(TaskRunningResultStatistics.class));
    }
    
    @Test
    public void assertExecute() throws SchedulerException {
        for (StatisticInterval each : StatisticInterval.values()) {
            taskRunningResultStatisticJob.setStatisticInterval(each);
            Optional<TaskRunningResultStatistics> latestOne = Optional.of(new TaskRunningResultStatistics(0, 0, each, StatisticTimeUtils.getStatisticTime(each, -3)));
            when(repository.findLatestTaskRunningResultStatistics(each)).thenReturn(latestOne);
            when(repository.add(any(TaskRunningResultStatistics.class))).thenReturn(true);
            taskRunningResultStatisticJob.execute(null);
            verify(repository).findLatestTaskRunningResultStatistics(each);
        }
        verify(repository, times(StatisticInterval.values().length * 3)).add(any(TaskRunningResultStatistics.class));
    }
}
