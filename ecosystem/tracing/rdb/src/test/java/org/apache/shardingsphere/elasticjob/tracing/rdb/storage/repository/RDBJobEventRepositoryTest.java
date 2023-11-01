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

package org.apache.shardingsphere.elasticjob.tracing.rdb.storage.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.spi.executor.ExecutionType;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.spi.tracing.event.JobStatusTraceEvent.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RDBJobEventRepositoryTest {
    
    private RDBJobEventRepository repository;
    
    private HikariDataSource dataSource;
    
    @BeforeEach
    void setup() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        repository = RDBJobEventRepository.getInstance(dataSource);
    }
    
    @AfterEach
    void tearDown() {
        dataSource.close();
    }
    
    @Test
    void assertAddJobExecutionEvent() {
        assertTrue(repository.addJobExecutionEvent(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0)));
    }
    
    @Test
    void assertAddJobStatusTraceEvent() {
        assertTrue(repository.addJobStatusTraceEvent(
                new JobStatusTraceEvent("test_job", "fake_task_id", "fake_slave_id", ExecutionType.READY, "0", State.TASK_RUNNING, "message is empty.")));
    }
    
    @Test
    void assertUpdateJobExecutionEventWhenSuccess() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(repository.addJobExecutionEvent(startEvent));
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        assertTrue(repository.addJobExecutionEvent(successEvent));
    }
    
    @Test
    void assertUpdateJobExecutionEventWhenFailure() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(repository.addJobExecutionEvent(startEvent));
        JobExecutionEvent failureEvent = startEvent.executionFailure("java.lang.RuntimeException: failure");
        assertTrue(repository.addJobExecutionEvent(failureEvent));
        assertThat(failureEvent.getFailureCause(), is("java.lang.RuntimeException: failure"));
        assertNotNull(failureEvent.getCompleteTime());
    }
    
    @Test
    void assertUpdateJobExecutionEventWhenSuccessAndConflict() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        assertTrue(repository.addJobExecutionEvent(successEvent));
        assertFalse(repository.addJobExecutionEvent(startEvent));
    }
    
    @Test
    void assertUpdateJobExecutionEventWhenFailureAndConflict() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent failureEvent = startEvent.executionFailure("java.lang.RuntimeException: failure");
        assertTrue(repository.addJobExecutionEvent(failureEvent));
        assertThat(failureEvent.getFailureCause(), is("java.lang.RuntimeException: failure"));
        assertFalse(repository.addJobExecutionEvent(startEvent));
    }
    
    @Test
    void assertUpdateJobExecutionEventWhenFailureAndMessageExceed() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(repository.addJobExecutionEvent(startEvent));
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            failureMsg.append(i);
        }
        JobExecutionEvent failEvent = startEvent.executionFailure("java.lang.RuntimeException: failure" + failureMsg);
        assertTrue(repository.addJobExecutionEvent(failEvent));
        assertThat(failEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
    
    @Test
    void assertFindJobExecutionEvent() {
        repository.addJobExecutionEvent(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
    }
}
