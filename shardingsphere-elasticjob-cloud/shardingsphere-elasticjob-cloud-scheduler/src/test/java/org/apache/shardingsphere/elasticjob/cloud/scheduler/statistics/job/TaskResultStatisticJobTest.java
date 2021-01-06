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

import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.TaskResultMetaData;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.util.StatisticTimeUtils;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.rdb.StatisticRdbRepository;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Trigger;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskResultStatisticJobTest {
    
    private final StatisticInterval statisticInterval = StatisticInterval.MINUTE;
    
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
    public void assertBuildTrigger() {
        for (StatisticInterval each : StatisticInterval.values()) {
            taskResultStatisticJob.setStatisticInterval(each);
            Trigger trigger = taskResultStatisticJob.buildTrigger();
            assertThat(trigger.getKey().getName(), is(TaskResultStatisticJob.class.getSimpleName() + "Trigger" + "_" + each));
        }
    }
    
    @Test
    public void assertGetDataMap() {
        assertThat(taskResultStatisticJob.getDataMap().get("statisticInterval"), is(statisticInterval));
        assertThat(taskResultStatisticJob.getDataMap().get("sharedData"), is(sharedData));
        assertThat(taskResultStatisticJob.getDataMap().get("repository"), is(repository));
    }
    
    @Test
    public void assertExecuteWhenRepositoryIsEmpty() {
        Optional<TaskResultStatistics> latestOne = Optional.empty();
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
    public void assertExecute() {
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
