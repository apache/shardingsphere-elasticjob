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

package com.dangdang.ddframe.job.statistics.rdb;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.job.statistics.type.JobRegisterStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics;
import com.dangdang.ddframe.job.statistics.type.TaskRunningResultStatistics.StatisticUnit;
import com.dangdang.ddframe.job.statistics.type.TaskRunningStatistics;
import com.google.common.base.Optional;

public class StatisticRdbRepositoryTest {
    
    private StatisticRdbRepository  repository;
    
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
    public void assertAddTaskRunningResultStatistics() {
        for (StatisticUnit each : StatisticUnit.values()) {
            assertTrue(repository.add(new TaskRunningResultStatistics(100, 0, each, new Date())));
        }
    }
    
    @Test
    public void assertAddTaskRunningStatistics() {
        assertTrue(repository.add(new TaskRunningStatistics(100, new Date())));
    }
    
    @Test
    public void assertAddJobRegisterStatistics() {
        assertTrue(repository.add(new JobRegisterStatistics(100, new Date())));
    }
    
    @Test
    public void assertFindTaskRunningResultStatisticsWhenTableIsEmpty() {
        assertThat(repository.findTaskRunningResultStatistics(new Date(), StatisticUnit.MINUTE).size(), is(0));
        assertThat(repository.findTaskRunningResultStatistics(new Date(), StatisticUnit.HOUR).size(), is(0));
        assertThat(repository.findTaskRunningResultStatistics(new Date(), StatisticUnit.DAY).size(), is(0));
    }
    
    @Test
    public void assertFindTaskRunningResultStatisticsWithDifferentFromDate() {
        Date now = new Date();
        Date yesterday = getYesterday();
        for (StatisticUnit each : StatisticUnit.values()) {
            assertTrue(repository.add(new TaskRunningResultStatistics(100, 0, each, yesterday)));
            assertTrue(repository.add(new TaskRunningResultStatistics(100, 0, each, now)));
            assertThat(repository.findTaskRunningResultStatistics(yesterday, each).size(), is(2));
            assertThat(repository.findTaskRunningResultStatistics(now, each).size(), is(1));
        }
    }
    
    @Test
    public void assertGetSummedTaskRunningResultStatisticsWhenTableIsEmpty() {
        for (StatisticUnit each : StatisticUnit.values()) {
            TaskRunningResultStatistics po = repository.getSummedTaskRunningResultStatistics(new Date(), each);
            assertThat(po.getSuccessCount(), is(0));
            assertThat(po.getFailedCount(), is(0));
        }
    }
    
    @Test
    public void assertGetSummedTaskRunningResultStatistics() {
        for (StatisticUnit each : StatisticUnit.values()) {
            repository.add(new TaskRunningResultStatistics(100, 2, each, new Date()));
            repository.add(new TaskRunningResultStatistics(200, 5, each, new Date()));
            TaskRunningResultStatistics po = repository.getSummedTaskRunningResultStatistics(new Date(), each);
            assertThat(po.getSuccessCount(), is(300));
            assertThat(po.getFailedCount(), is(7));
        }
    }
    
    @Test
    public void assertFindLatestTaskRunningResultStatisticsWhenTableIsEmpty() {
        for (StatisticUnit each : StatisticUnit.values()) {
            assertFalse(repository.findLatestTaskRunningResultStatistics(each).isPresent());
        }
    }
    
    @Test
    public void assertFindLatestTaskRunningResultStatistics() {
        for (StatisticUnit each : StatisticUnit.values()) {
            repository.add(new TaskRunningResultStatistics(100, 2, each, new Date()));
            repository.add(new TaskRunningResultStatistics(200, 5, each, new Date()));
            Optional<TaskRunningResultStatistics> po = repository.findLatestTaskRunningResultStatistics(each);
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
