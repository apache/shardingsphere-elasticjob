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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.job;

import org.apache.shardingsphere.elasticjob.cloud.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class JobRunningStatisticJobTest {
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private JobRunningStatisticJob jobRunningStatisticJob;
    
    @Before
    public void setUp() {
        jobRunningStatisticJob = new JobRunningStatisticJob();
        jobRunningStatisticJob.setRunningService(runningService);
        jobRunningStatisticJob.setRepository(repository);
    }
    
    @Test
    public void assertBuildJobDetail() {
        Assert.assertThat(jobRunningStatisticJob.buildJobDetail().getKey().getName(), Is.is(JobRunningStatisticJob.class.getSimpleName()));
    }
    
    @Test
    public void assertBuildTrigger() {
        Trigger trigger = jobRunningStatisticJob.buildTrigger();
        Assert.assertThat(trigger.getKey().getName(), Is.is(JobRunningStatisticJob.class.getSimpleName() + "Trigger"));
    }
    
    @Test
    public void assertGetDataMap() {
        Assert.assertThat(jobRunningStatisticJob.getDataMap().get("runningService"), Is.is(runningService));
        Assert.assertThat(jobRunningStatisticJob.getDataMap().get("repository"), Is.is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() throws SchedulerException {
        Optional<JobRunningStatistics> latestJobRunningStatistics = Optional.empty();
        Optional<TaskRunningStatistics> latestTaskRunningStatistics = Optional.empty();
        Mockito.when(repository.findLatestJobRunningStatistics()).thenReturn(latestJobRunningStatistics);
        Mockito.when(repository.findLatestTaskRunningStatistics()).thenReturn(latestTaskRunningStatistics);
        Mockito.when(repository.add(ArgumentMatchers.any(JobRunningStatistics.class))).thenReturn(true);
        Mockito.when(repository.add(ArgumentMatchers.any(TaskRunningStatistics.class))).thenReturn(true);
        Mockito.when(runningService.getAllRunningTasks()).thenReturn(Collections.emptyMap());
        jobRunningStatisticJob.execute(null);
        Mockito.verify(repository).findLatestJobRunningStatistics();
        Mockito.verify(repository).add(ArgumentMatchers.any(JobRunningStatistics.class));
        Mockito.verify(repository).add(ArgumentMatchers.any(TaskRunningStatistics.class));
        Mockito.verify(runningService).getAllRunningTasks();
    }
    
    @Test
    public void assertExecute() throws SchedulerException {
        Optional<JobRunningStatistics> latestJobRunningStatistics = Optional.of(new JobRunningStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.MINUTE, -3)));
        Optional<TaskRunningStatistics> latestTaskRunningStatistics = Optional.of(new TaskRunningStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.MINUTE, -3)));
        Mockito.when(repository.findLatestJobRunningStatistics()).thenReturn(latestJobRunningStatistics);
        Mockito.when(repository.findLatestTaskRunningStatistics()).thenReturn(latestTaskRunningStatistics);
        Mockito.when(repository.add(ArgumentMatchers.any(JobRunningStatistics.class))).thenReturn(true);
        Mockito.when(repository.add(ArgumentMatchers.any(TaskRunningStatistics.class))).thenReturn(true);
        Map<String, Set<TaskContext>> jobMap = new HashMap<>(1);
        Set<TaskContext> jobSet = new HashSet<>(1);
        jobSet.add(TaskContext.from(TaskNode.builder().jobName("test_job").build().getTaskNodeValue()));
        jobMap.put("test_job", jobSet);
        Mockito.when(runningService.getAllRunningTasks()).thenReturn(jobMap);
        jobRunningStatisticJob.execute(null);
        Mockito.verify(repository).findLatestJobRunningStatistics();
        Mockito.verify(repository).findLatestTaskRunningStatistics();
        Mockito.verify(repository, Mockito.times(3)).add(ArgumentMatchers.any(JobRunningStatistics.class));
        Mockito.verify(repository, Mockito.times(3)).add(ArgumentMatchers.any(TaskRunningStatistics.class));
        Mockito.verify(runningService).getAllRunningTasks();
    }
}
