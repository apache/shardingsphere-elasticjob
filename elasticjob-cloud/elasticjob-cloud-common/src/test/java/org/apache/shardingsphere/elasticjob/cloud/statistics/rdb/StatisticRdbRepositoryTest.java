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

package org.apache.shardingsphere.elasticjob.cloud.statistics.rdb;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.elasticjob.cloud.statistics.StatisticInterval;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRegisterStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.job.JobRunningStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskResultStatistics;
import org.apache.shardingsphere.elasticjob.cloud.statistics.type.task.TaskRunningStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticRdbRepositoryTest {
    
    private StatisticRdbRepository repository;
    
    @BeforeEach
    void setup() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        repository = new StatisticRdbRepository(dataSource);
    }
    
    @Test
    void assertAddTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            assertTrue(repository.add(new TaskResultStatistics(100, 0, each, new Date())));
        }
    }
    
    @Test
    void assertAddTaskRunningStatistics() {
        assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    void assertAddJobRunningStatistics() {
        assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    void assertAddJobRegisterStatistics() {
        assertTrue(repository.add(new JobRegisterStatistics(100, new Date())));
    }
    
    @Test
    void assertFindTaskResultStatisticsWhenTableIsEmpty() {
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.MINUTE).size(), is(0));
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.HOUR).size(), is(0));
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.DAY).size(), is(0));
    }
    
    @Test
    void assertFindTaskResultStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        for (StatisticInterval each : StatisticInterval.values()) {
            assertTrue(repository.add(new TaskResultStatistics(100, 0, each, yesterday)));
            assertTrue(repository.add(new TaskResultStatistics(100, 0, each, now)));
            assertThat(repository.findTaskResultStatistics(yesterday, each).size(), is(2));
            assertThat(repository.findTaskResultStatistics(now, each).size(), is(1));
        }
    }
    
    @Test
    void assertGetSummedTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics po = repository.getSummedTaskResultStatistics(new Date(), each);
            assertThat(po.getSuccessCount(), is(0));
            assertThat(po.getFailedCount(), is(0));
        }
    }
    
    @Test
    void assertGetSummedTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            Date date = new Date();
            repository.add(new TaskResultStatistics(100, 2, each, date));
            repository.add(new TaskResultStatistics(200, 5, each, date));
            TaskResultStatistics po = repository.getSummedTaskResultStatistics(date, each);
            assertThat(po.getSuccessCount(), is(300));
            assertThat(po.getFailedCount(), is(7));
        }
    }
    
    @Test
    void assertFindLatestTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            assertFalse(repository.findLatestTaskResultStatistics(each).isPresent());
        }
    }
    
    @Test
    void assertFindLatestTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            repository.add(new TaskResultStatistics(100, 2, each, new Date()));
            repository.add(new TaskResultStatistics(200, 5, each, new Date()));
            Optional<TaskResultStatistics> po = repository.findLatestTaskResultStatistics(each);
            assertTrue(po.isPresent());
            assertThat(po.get().getSuccessCount(), is(200));
            assertThat(po.get().getFailedCount(), is(5));
        }
    }
    
    @Test
    void assertFindTaskRunningStatisticsWhenTableIsEmpty() {
        assertThat(repository.findTaskRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    void assertFindTaskRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new TaskRunningStatistics(100, yesterday)));
        assertTrue(repository.add(new TaskRunningStatistics(100, now)));
        assertThat(repository.findTaskRunningStatistics(yesterday).size(), is(2));
        assertThat(repository.findTaskRunningStatistics(now).size(), is(1));
    }
    
    @Test
    void assertFindLatestTaskRunningStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestTaskRunningStatistics().isPresent());
    }
    
    @Test
    void assertFindLatestTaskRunningStatistics() {
        repository.add(new TaskRunningStatistics(100, new Date()));
        repository.add(new TaskRunningStatistics(200, new Date()));
        Optional<TaskRunningStatistics> po = repository.findLatestTaskRunningStatistics();
        assertTrue(po.isPresent());
        assertThat(po.get().getRunningCount(), is(200));
    }
    
    @Test
    void assertFindJobRunningStatisticsWhenTableIsEmpty() {
        assertThat(repository.findJobRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    void assertFindJobRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new JobRunningStatistics(100, yesterday)));
        assertTrue(repository.add(new JobRunningStatistics(100, now)));
        assertThat(repository.findJobRunningStatistics(yesterday).size(), is(2));
        assertThat(repository.findJobRunningStatistics(now).size(), is(1));
    }
    
    @Test
    void assertFindLatestJobRunningStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestJobRunningStatistics().isPresent());
    }
    
    @Test
    void assertFindLatestJobRunningStatistics() {
        repository.add(new JobRunningStatistics(100, new Date()));
        repository.add(new JobRunningStatistics(200, new Date()));
        Optional<JobRunningStatistics> po = repository.findLatestJobRunningStatistics();
        assertTrue(po.isPresent());
        assertThat(po.get().getRunningCount(), is(200));
    }
    
    @Test
    void assertFindJobRegisterStatisticsWhenTableIsEmpty() {
        assertThat(repository.findJobRegisterStatistics(new Date()).size(), is(0));
    }
    
    @Test
    void assertFindJobRegisterStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new JobRegisterStatistics(100, yesterday)));
        assertTrue(repository.add(new JobRegisterStatistics(100, now)));
        assertThat(repository.findJobRegisterStatistics(yesterday).size(), is(2));
        assertThat(repository.findJobRegisterStatistics(now).size(), is(1));
    }
    
    @Test
    void assertFindLatestJobRegisterStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestJobRegisterStatistics().isPresent());
    }
    
    @Test
    void assertFindLatestJobRegisterStatistics() {
        repository.add(new JobRegisterStatistics(100, new Date()));
        repository.add(new JobRegisterStatistics(200, new Date()));
        Optional<JobRegisterStatistics> po = repository.findLatestJobRegisterStatistics();
        assertTrue(po.isPresent());
        assertThat(po.get().getRegisteredCount(), is(200));
    }
    
    private Date getYesterday() {
        return new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
    }
}
