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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.TaskNode;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRunningStatisticJobTest {
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private StatisticRdbRepository repository;
    
    private JobRunningStatisticJob jobRunningStatisticJob;
    
    @BeforeEach
    void setUp() {
        jobRunningStatisticJob = new JobRunningStatisticJob();
        jobRunningStatisticJob.setRunningService(runningService);
        jobRunningStatisticJob.setRepository(repository);
    }
    
    @Test
    void assertBuildJobDetail() {
        assertThat(jobRunningStatisticJob.buildJobDetail().getKey().getName(), is(JobRunningStatisticJob.class.getSimpleName()));
    }
    
    @Test
    void assertBuildTrigger() {
        Trigger trigger = jobRunningStatisticJob.buildTrigger();
        assertThat(trigger.getKey().getName(), is(JobRunningStatisticJob.class.getSimpleName() + "Trigger"));
    }
    
    @Test
    void assertGetDataMap() {
        assertThat(jobRunningStatisticJob.getDataMap().get("runningService"), is(runningService));
        assertThat(jobRunningStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    void assertExecuteWhenRepositoryIsEmpty() {
        Optional<JobRunningStatistics> latestJobRunningStatistics = Optional.empty();
        Optional<TaskRunningStatistics> latestTaskRunningStatistics = Optional.empty();
        when(repository.findLatestJobRunningStatistics()).thenReturn(latestJobRunningStatistics);
        when(repository.findLatestTaskRunningStatistics()).thenReturn(latestTaskRunningStatistics);
        when(repository.add(ArgumentMatchers.any(JobRunningStatistics.class))).thenReturn(true);
        when(repository.add(ArgumentMatchers.any(TaskRunningStatistics.class))).thenReturn(true);
        when(runningService.getAllRunningTasks()).thenReturn(Collections.emptyMap());
        jobRunningStatisticJob.execute(null);
        verify(repository).findLatestJobRunningStatistics();
        verify(repository).add(ArgumentMatchers.any(JobRunningStatistics.class));
        verify(repository).add(ArgumentMatchers.any(TaskRunningStatistics.class));
        verify(runningService).getAllRunningTasks();
    }
    
    @Test
    void assertExecute() {
        Optional<JobRunningStatistics> latestJobRunningStatistics = Optional.of(new JobRunningStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.MINUTE, -3)));
        Optional<TaskRunningStatistics> latestTaskRunningStatistics = Optional.of(new TaskRunningStatistics(0, StatisticTimeUtils.getStatisticTime(StatisticInterval.MINUTE, -3)));
        when(repository.findLatestJobRunningStatistics()).thenReturn(latestJobRunningStatistics);
        when(repository.findLatestTaskRunningStatistics()).thenReturn(latestTaskRunningStatistics);
        when(repository.add(ArgumentMatchers.any(JobRunningStatistics.class))).thenReturn(true);
        when(repository.add(ArgumentMatchers.any(TaskRunningStatistics.class))).thenReturn(true);
        Map<String, Set<TaskContext>> jobMap = new HashMap<>(1);
        Set<TaskContext> jobSet = new HashSet<>(1);
        jobSet.add(TaskContext.from(TaskNode.builder().jobName("test_job").build().getTaskNodeValue()));
        jobMap.put("test_job", jobSet);
        when(runningService.getAllRunningTasks()).thenReturn(jobMap);
        jobRunningStatisticJob.execute(null);
        verify(repository).findLatestJobRunningStatistics();
        verify(repository).findLatestTaskRunningStatistics();
        verify(repository, times(3)).add(ArgumentMatchers.any(JobRunningStatistics.class));
        verify(repository, times(3)).add(ArgumentMatchers.any(TaskRunningStatistics.class));
        verify(runningService).getAllRunningTasks();
    }
}
