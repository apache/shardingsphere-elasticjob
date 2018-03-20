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

package io.elasticjob.lite.statistics.rdb;

import com.google.common.base.Optional;
import io.elasticjob.lite.statistics.StatisticInterval;
import io.elasticjob.lite.statistics.type.job.JobRegisterStatistics;
import io.elasticjob.lite.statistics.type.job.JobRunningStatistics;
import io.elasticjob.lite.statistics.type.task.TaskResultStatistics;
import io.elasticjob.lite.statistics.type.task.TaskRunningStatistics;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class StatisticRdbRepositoryTest {
    
    private StatisticRdbRepository repository;
    
    @Before
    public void setup() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        repository = new StatisticRdbRepository(dataSource);
    }
    
    @Test
    public void assertAddTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            assertTrue(repository.add(new TaskResultStatistics(100, 0, each, new Date())));
        }
    }
    
    @Test
    public void assertAddTaskRunningStatistics() {
        assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    public void assertAddJobRunningStatistics() {
        assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    public void assertAddJobRegisterStatistics() {
        assertTrue(repository.add(new JobRegisterStatistics(100, new Date())));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWhenTableIsEmpty() {
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.MINUTE).size(), is(0));
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.HOUR).size(), is(0));
        assertThat(repository.findTaskResultStatistics(new Date(), StatisticInterval.DAY).size(), is(0));
    }
    
    @Test
    public void assertFindTaskResultStatisticsWithDifferentFromDate() {
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
    public void assertGetSummedTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            TaskResultStatistics po = repository.getSummedTaskResultStatistics(new Date(), each);
            assertThat(po.getSuccessCount(), is(0));
            assertThat(po.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertGetSummedTaskResultStatistics() {
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
    public void assertFindLatestTaskResultStatisticsWhenTableIsEmpty() {
        for (StatisticInterval each : StatisticInterval.values()) {
            assertFalse(repository.findLatestTaskResultStatistics(each).isPresent());
        }
    }
    
    @Test
    public void assertFindLatestTaskResultStatistics() {
        for (StatisticInterval each : StatisticInterval.values()) {
            repository.add(new TaskResultStatistics(100, 2, each, new Date()));
            repository.add(new TaskResultStatistics(200, 5, each, new Date()));
            Optional<TaskResultStatistics> po = repository.findLatestTaskResultStatistics(each);
            assertThat(po.get().getSuccessCount(), is(200));
            assertThat(po.get().getFailedCount(), is(5));
        }
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWhenTableIsEmpty() {
        assertThat(repository.findTaskRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new TaskRunningStatistics(100, yesterday)));
        assertTrue(repository.add(new TaskRunningStatistics(100, now)));
        assertThat(repository.findTaskRunningStatistics(yesterday).size(), is(2));
        assertThat(repository.findTaskRunningStatistics(now).size(), is(1));
    }
    
    @Test
    public void assertFindLatestTaskRunningStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestTaskRunningStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestTaskRunningStatistics() {
        repository.add(new TaskRunningStatistics(100, new Date()));
        repository.add(new TaskRunningStatistics(200, new Date()));
        Optional<TaskRunningStatistics> po = repository.findLatestTaskRunningStatistics();
        assertThat(po.get().getRunningCount(), is(200));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWhenTableIsEmpty() {
        assertThat(repository.findJobRunningStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindJobRunningStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new JobRunningStatistics(100, yesterday)));
        assertTrue(repository.add(new JobRunningStatistics(100, now)));
        assertThat(repository.findJobRunningStatistics(yesterday).size(), is(2));
        assertThat(repository.findJobRunningStatistics(now).size(), is(1));
    }
    
    @Test
    public void assertFindLatestJobRunningStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestJobRunningStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestJobRunningStatistics() {
        repository.add(new JobRunningStatistics(100, new Date()));
        repository.add(new JobRunningStatistics(200, new Date()));
        Optional<JobRunningStatistics> po = repository.findLatestJobRunningStatistics();
        assertThat(po.get().getRunningCount(), is(200));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWhenTableIsEmpty() {
        assertThat(repository.findJobRegisterStatistics(new Date()).size(), is(0));
    }
    
    @Test
    public void assertFindJobRegisterStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        assertTrue(repository.add(new JobRegisterStatistics(100, yesterday)));
        assertTrue(repository.add(new JobRegisterStatistics(100, now)));
        assertThat(repository.findJobRegisterStatistics(yesterday).size(), is(2));
        assertThat(repository.findJobRegisterStatistics(now).size(), is(1));
    }
    
    @Test
    public void assertFindLatestJobRegisterStatisticsWhenTableIsEmpty() {
        assertFalse(repository.findLatestJobRegisterStatistics().isPresent());
    }
    
    @Test
    public void assertFindLatestJobRegisterStatistics() {
        repository.add(new JobRegisterStatistics(100, new Date()));
        repository.add(new JobRegisterStatistics(200, new Date()));
        Optional<JobRegisterStatistics> po = repository.findLatestJobRegisterStatistics();
        assertThat(po.get().getRegisteredCount(), is(200));
    }
    
    private Date getYesterday() {
        return new Date(new Date().getTime() - 24 * 60 * 60 * 1000);
    }
}
