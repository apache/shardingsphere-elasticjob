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

package org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.rdb;

import org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.JobRegisterStatisticsRepository;
import org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.JobRunningStatisticsRepository;
import org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.TaskResultStatisticsRepository;
import org.apache.shardingsphere.elasticjob.lite.console.dao.statistics.TaskRunningStatisticsRepository;
import org.apache.shardingsphere.elasticjob.lite.console.domain.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.lite.console.domain.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.lite.console.domain.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.lite.console.domain.TaskRunningStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RDBStatisticRepositoryTest {
    
    @Autowired
    private TaskResultStatisticsRepository taskResultStatisticsRepository;
    
    @Autowired
    private TaskRunningStatisticsRepository taskRunningStatisticsRepository;
    
    @Autowired
    private JobRegisterStatisticsRepository jobRegisterStatisticsRepository;
    
    @Autowired
    private JobRunningStatisticsRepository jobRunningStatisticsRepository;
    
    @Test
    public void assertAddTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics taskResultStatistics = new TaskResultStatistics(100L, 0L, each.name(), new Date());
            assertTrue(taskResultStatistics.equals(taskResultStatisticsRepository.save(taskResultStatistics)));
        }
    }
    
    @Test
    public void assertAddTaskRunningStatistics() {
        TaskRunningStatistics taskRunningStatistics = new TaskRunningStatistics(100, new Date());
        assertTrue(taskRunningStatistics.equals(taskRunningStatisticsRepository.save(taskRunningStatistics)));
    }
    
    @Test
    public void assertAddJobRunningStatistics() {
        JobRunningStatistics jobRunningStatistics = new JobRunningStatistics(100, new Date());
        assertTrue(jobRunningStatistics.equals(jobRunningStatisticsRepository.save(jobRunningStatistics)));
    }
    
    @Test
    public void assertAddJobRegisterStatistics() {
        JobRegisterStatistics jobRegisterStatistics = new JobRegisterStatistics(100, new Date());
        assertTrue(jobRegisterStatistics.equals(jobRegisterStatisticsRepository.save(jobRegisterStatistics)));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWhenTableIsEmpty() {
        Date now = new Date();
        assertThat(taskResultStatisticsRepository.findTaskResultStatistics(now, StatisticInterval.MINUTE.name()).size(), is(0));
        assertThat(taskResultStatisticsRepository.findTaskResultStatistics(now, StatisticInterval.HOUR.name()).size(), is(0));
        assertThat(taskResultStatisticsRepository.findTaskResultStatistics(now, StatisticInterval.DAY.name()).size(), is(0));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        for (StatisticInterval each : StatisticInterval.values()) {
            taskResultStatisticsRepository.save(new TaskResultStatistics(100L, 0L, each.name(), yesterday));
            taskResultStatisticsRepository.save(new TaskResultStatistics(100L, 0L, each.name(), now));
            assertThat(taskResultStatisticsRepository.findTaskResultStatistics(yesterday, each.name()).size(), is(2));
            assertThat(taskResultStatisticsRepository.findTaskResultStatistics(now, each.name()).size(), is(1));
        }
    }
    
    @Test
    public void assertGetSummedTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics po = taskResultStatisticsRepository.getSummedTaskResultStatistics(new Date(), each.name());
            assertThat(po.getSuccessCount(), nullValue());
            assertThat(po.getFailedCount(), nullValue());
        }
    }
    
    @Test
    public void assertGetSummedTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            Date date = new Date();
            taskResultStatisticsRepository.save(new TaskResultStatistics(100L, 2L, each.name(), date));
            taskResultStatisticsRepository.save(new TaskResultStatistics(200L, 5L, each.name(), date));
            TaskResultStatistics po = taskResultStatisticsRepository.getSummedTaskResultStatistics(date, each.name());
            assertThat(po.getSuccessCount(), is(300L));
            assertThat(po.getFailedCount(), is(7L));
        }
    }

    @Test
    public void assertFindTaskRunningStatisticsWhenTableIsEmpty() {
        assertThat(taskRunningStatisticsRepository.findTaskRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        taskRunningStatisticsRepository.deleteAll();
        taskRunningStatisticsRepository.save(new TaskRunningStatistics(100, yesterday));
        taskRunningStatisticsRepository.save(new TaskRunningStatistics(100, now));
        assertThat(taskRunningStatisticsRepository.findTaskRunningStatistics(yesterday).size(), is(2));
        assertThat(taskRunningStatisticsRepository.findTaskRunningStatistics(now).size(), is(1));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenTableIsEmpty() {
        assertThat(jobRunningStatisticsRepository.findJobRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        jobRunningStatisticsRepository.deleteAll();
        jobRunningStatisticsRepository.save(new JobRunningStatistics(100, yesterday));
        jobRunningStatisticsRepository.save(new JobRunningStatistics(100, now));
        assertThat(jobRunningStatisticsRepository.findJobRunningStatistics(yesterday).size(), is(2));
        assertThat(jobRunningStatisticsRepository.findJobRunningStatistics(now).size(), is(1));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenTableIsEmpty() {
        assertThat(jobRegisterStatisticsRepository.findJobRegisterStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        jobRegisterStatisticsRepository.save(new JobRegisterStatistics(100, yesterday));
        jobRegisterStatisticsRepository.save(new JobRegisterStatistics(100, now));
        assertThat(jobRegisterStatisticsRepository.findJobRegisterStatistics(yesterday).size(), is(2));
        assertThat(jobRegisterStatisticsRepository.findJobRegisterStatistics(now).size(), is(1));
    }
    
    private Date getYesterday() {
        return new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
    }
}
