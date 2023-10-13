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

package org.apache.shardingsphere.elasticjob.cloud.console.controller.search;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobEventRdbSearchTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    // TODO We should not use `Mock.Strictness.LENIENT` here, but the default. This is a flaw in the unit test design.
    @Mock(strictness = Mock.Strictness.LENIENT)
    private ResultSet resultSet;
    
    @Mock
    private Connection conn;
    
    private JobEventRdbSearch.Condition condition;
    
    private JobEventRdbSearch jobEventRdbSearch;
    
    @BeforeEach
    void setUp() throws Exception {
        jobEventRdbSearch = new JobEventRdbSearch(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getInt(1)).thenReturn(1);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    }
    
    @Test
    @SneakyThrows
    void assertFindJobExecutionEvents() {
        when(resultSet.getString(5)).thenReturn("TestJobName");
        when(resultSet.getString(6)).thenReturn("FAILOVER");
        when(resultSet.getString(7)).thenReturn("1");
        when(resultSet.getTimestamp(8)).thenReturn(new Timestamp(System.currentTimeMillis()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("job_name", "TestJobName");
        condition = new JobEventRdbSearch.Condition(1, 10, "job_name", "ASC", new Date(), new Date(), fields);
        JobEventRdbSearch.Result<JobExecutionEvent> jobExecutionEvents = jobEventRdbSearch.findJobExecutionEvents(condition);
        assertThat(jobExecutionEvents.getTotal(), is(1));
        assertThat(jobExecutionEvents.getRows().size(), is(1));
        assertThat(jobExecutionEvents.getRows().get(0).getJobName(), is("TestJobName"));
        assertThat(jobExecutionEvents.getRows().get(0).getSource(), is(JobExecutionEvent.ExecutionSource.FAILOVER));
        assertThat(jobExecutionEvents.getRows().get(0).getShardingItem(), is(1));
    }
    
    @Test
    @SneakyThrows
    void assertFindJobStatusTraceEvents() {
        when(resultSet.getString(2)).thenReturn("TestJobName");
        when(resultSet.getString(6)).thenReturn("LITE_EXECUTOR");
        when(resultSet.getString(9)).thenReturn("TASK_RUNNING");
        when(resultSet.getTimestamp(11)).thenReturn(new Timestamp(System.currentTimeMillis()));
        Map<String, Object> fields = new HashMap<>();
        fields.put("job_name", "TestJobName");
        condition = new JobEventRdbSearch.Condition(0, 0, "job_name", "DESC", new Date(), new Date(), fields);
        JobEventRdbSearch.Result<JobStatusTraceEvent> jobStatusTraceEvents = jobEventRdbSearch.findJobStatusTraceEvents(condition);
        assertThat(jobStatusTraceEvents.getTotal(), is(1));
        assertThat(jobStatusTraceEvents.getRows().size(), is(1));
        assertThat(jobStatusTraceEvents.getRows().get(0).getJobName(), is("TestJobName"));
        assertThat(jobStatusTraceEvents.getRows().get(0).getSource(), is(JobStatusTraceEvent.Source.LITE_EXECUTOR));
        assertThat(jobStatusTraceEvents.getRows().get(0).getState(), is(JobStatusTraceEvent.State.TASK_RUNNING));
    }
}
