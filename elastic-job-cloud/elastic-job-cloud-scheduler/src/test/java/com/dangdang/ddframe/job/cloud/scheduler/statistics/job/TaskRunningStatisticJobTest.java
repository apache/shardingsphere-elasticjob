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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.dangdang.ddframe.job.cloud.scheduler.fixture.TaskNode;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.Interval;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.util.StatisticTimeUtils;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.statistics.rdb.StatisticRdbRepository;
import com.dangdang.ddframe.job.statistics.type.TaskRunningStatistics;
import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class TaskRunningStatisticJobTest {
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private TaskRunningStatisticJob taskRunningStatisticJob;
    
    @Before
    public void setUp() {
        taskRunningStatisticJob = new TaskRunningStatisticJob();
        taskRunningStatisticJob.setRunningService(runningService);
        taskRunningStatisticJob.setRepository(repository);
    }
    
    @Test
    public void assertBuildJobDetail() {
        assertThat(taskRunningStatisticJob.buildJobDetail().getKey().getName(), is(TaskRunningStatisticJob.class.getSimpleName()));
    }
    
    @Test
    public void assertBuildTrigger() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        Trigger trigger = taskRunningStatisticJob.buildTrigger();
        scheduler.scheduleJob(taskRunningStatisticJob.buildJobDetail(), trigger);
        assertThat(trigger.getKey().getName(), is(TaskRunningStatisticJob.class.getSimpleName() + "Trigger"));
        assertThat(trigger.getNextFireTime(), is(StatisticTimeUtils.getStatisticTime(Interval.MINUTE, 1)));
    }
    
    @Test
    public void assertGetDataMap() throws SchedulerException {
        assertThat((RunningService) taskRunningStatisticJob.getDataMap().get("runningService"), is(runningService));
        assertThat((StatisticRdbRepository) taskRunningStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() throws SchedulerException {
        Optional<TaskRunningStatistics> latestOne = Optional.absent();
        when(repository.findLatestTaskRunningStatistics()).thenReturn(latestOne);
        when(repository.add(any(TaskRunningStatistics.class))).thenReturn(true);
        when(runningService.getAllRunningTasks()).thenReturn(Collections.<String, Set<TaskContext>>emptyMap());
        taskRunningStatisticJob.execute(null);
        verify(repository).findLatestTaskRunningStatistics();
        verify(repository).add(any(TaskRunningStatistics.class));
        verify(runningService).getAllRunningTasks();
    }
    
    @Test
    public void assertExecute() throws SchedulerException {
        Optional<TaskRunningStatistics> latestOne = Optional.of(new TaskRunningStatistics(0, StatisticTimeUtils.getStatisticTime(Interval.MINUTE, -3)));
        when(repository.findLatestTaskRunningStatistics()).thenReturn(latestOne);
        when(repository.add(any(TaskRunningStatistics.class))).thenReturn(true);
        Map<String, Set<TaskContext>> jobMap = new HashMap<>(1);
        Set<TaskContext> taskSet = new HashSet<>(1);
        taskSet.add(TaskContext.from(TaskNode.builder().jobName("test_job").build().getTaskNodeValue()));
        jobMap.put("test_job", taskSet);
        when(runningService.getAllRunningTasks()).thenReturn(jobMap);
        taskRunningStatisticJob.execute(null);
        verify(repository).findLatestTaskRunningStatistics();
        verify(repository, times(3)).add(any(TaskRunningStatistics.class));
        verify(runningService).getAllRunningTasks();
    }
}
