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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.dangdang.ddframe.job.cloud.scheduler.statistics.TaskResultMetaData;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.statistics.StatisticInterval;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.task.TaskResultStatistics;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class TaskResultStatisticJobTest {
    
    private StatisticInterval statisticInterval = StatisticInterval.MINUTE;
    
    private TaskResultMetaData sharedData;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private TaskResultStatisticJob taskResultStatisticJob;
    
    @Before
    public void setUp() {
        taskResultStatisticJob = new TaskResultStatisticJob();
        sharedData = new TaskResultMetaData();
        taskResultStatisticJob.setStatisticInterval(statisticInterval);
        taskResultStatisticJob.setSharedData(sharedData);
        taskResultStatisticJob.setRepository(repository);
    }
    
    @Test
    public void assertBuildJobDetail() {
        assertThat(taskResultStatisticJob.buildJobDetail().getKey().getName(), is(TaskResultStatisticJob.class.getSimpleName() + "_" + statisticInterval));
    }
    
    @Test
    public void assertBuildTrigger() throws SchedulerException {
        for (StatisticInterval each : StatisticInterval.values()) {
            taskResultStatisticJob.setStatisticInterval(each);
            Trigger trigger = taskResultStatisticJob.buildTrigger();
            assertThat(trigger.getKey().getName(), is(TaskResultStatisticJob.class.getSimpleName() + "Trigger" + "_" + each));
        }
    }
    
    @Test
    public void assertGetDataMap() throws SchedulerException {
        assertThat((StatisticInterval) taskResultStatisticJob.getDataMap().get("statisticInterval"), is(statisticInterval));
        assertThat((TaskResultMetaData) taskResultStatisticJob.getDataMap().get("sharedData"), is(sharedData));
        assertThat((StatisticRdbRepository) taskResultStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() throws SchedulerException {
        Optional<TaskResultStatistics> latestOne = Optional.absent();
        for (StatisticInterval each : StatisticInterval.values()) {
            taskResultStatisticJob.setStatisticInterval(each);
            when(repository.findLatestTaskResultStatistics(each)).thenReturn(latestOne);
            when(repository.add(any(TaskResultStatistics.class))).thenReturn(true);
            taskResultStatisticJob.execute(null);
            verify(repository).findLatestTaskResultStatistics(each);
        }
        verify(repository, times(3)).add(any(TaskResultStatistics.class));
    }
    
    @Test
    public void assertExecute() throws SchedulerException {
        for (StatisticInterval each : StatisticInterval.values()) {
            taskResultStatisticJob.setStatisticInterval(each);
            Optional<TaskResultStatistics> latestOne = Optional.of(new TaskResultStatistics(0, 0, each, StatisticTimeUtils.getStatisticTime(each, -3)));
            when(repository.findLatestTaskResultStatistics(each)).thenReturn(latestOne);
            when(repository.add(any(TaskResultStatistics.class))).thenReturn(true);
            taskResultStatisticJob.execute(null);
            verify(repository).findLatestTaskResultStatistics(each);
        }
        verify(repository, times(StatisticInterval.values().length * 3)).add(any(TaskResultStatistics.class));
    }
}
