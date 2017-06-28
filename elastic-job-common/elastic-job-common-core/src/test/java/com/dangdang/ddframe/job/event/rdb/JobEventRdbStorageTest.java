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

package com.dangdang.ddframe.job.event.rdb;

import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.Source;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.State;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class JobEventRdbStorageTest {
    
    private JobEventRdbStorage storage;
    
    @Before
    public void setup() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        storage = new JobEventRdbStorage(dataSource);
    }
    
    @Test
    public void assertAddJobExecutionEvent() throws SQLException {
        assertTrue(storage.addJobExecutionEvent(new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0)));
    }
    
    @Test
    public void assertAddJobStatusTraceEvent() throws SQLException {
        assertTrue(storage.addJobStatusTraceEvent(new JobStatusTraceEvent("test_job", "fake_task_id", "fake_slave_id", Source.LITE_EXECUTOR, ExecutionType.READY, "0", 
                State.TASK_RUNNING, "message is empty.")));
    }
    
    @Test
    public void assertAddJobStatusTraceEventWhenFailoverWithTaskStagingState() throws SQLException {
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent("test_job", "fake_failover_task_id", "fake_slave_id", Source.LITE_EXECUTOR, ExecutionType.FAILOVER, "0",
                State.TASK_STAGING, "message is empty.");
        jobStatusTraceEvent.setOriginalTaskId("original_fake_failover_task_id");
        assertThat(storage.getJobStatusTraceEvents("fake_failover_task_id").size(), is(0));
        storage.addJobStatusTraceEvent(jobStatusTraceEvent);
        assertThat(storage.getJobStatusTraceEvents("fake_failover_task_id").size(), is(1));
    }
    
    @Test
    public void assertAddJobStatusTraceEventWhenFailoverWithTaskFailedState() throws SQLException {
        JobStatusTraceEvent stagingJobStatusTraceEvent = new JobStatusTraceEvent("test_job", "fake_failed_failover_task_id", "fake_slave_id", Source.LITE_EXECUTOR, ExecutionType.FAILOVER, "0",
                State.TASK_STAGING, "message is empty.");
        stagingJobStatusTraceEvent.setOriginalTaskId("original_fake_failed_failover_task_id");
        storage.addJobStatusTraceEvent(stagingJobStatusTraceEvent);
        JobStatusTraceEvent failedJobStatusTraceEvent = new JobStatusTraceEvent("test_job", "fake_failed_failover_task_id", "fake_slave_id", Source.LITE_EXECUTOR, ExecutionType.FAILOVER, "0",
                State.TASK_FAILED, "message is empty.");
        storage.addJobStatusTraceEvent(failedJobStatusTraceEvent);
        List<JobStatusTraceEvent> jobStatusTraceEvents = storage.getJobStatusTraceEvents("fake_failed_failover_task_id");
        assertThat(jobStatusTraceEvents.size(), is(2));
        for (JobStatusTraceEvent jobStatusTraceEvent : jobStatusTraceEvents) {
            assertThat(jobStatusTraceEvent.getOriginalTaskId(), is("original_fake_failed_failover_task_id"));
        }
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenSuccess() throws SQLException {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(startEvent));
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        assertTrue(storage.addJobExecutionEvent(successEvent));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailure() throws SQLException {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(startEvent));
        JobExecutionEvent failureEvent = startEvent.executionFailure(new RuntimeException("failure"));
        assertTrue(storage.addJobExecutionEvent(failureEvent));
        assertThat(failureEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
        assertTrue(null != failureEvent.getCompleteTime());
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenSuccessAndConflict() throws SQLException {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        assertTrue(storage.addJobExecutionEvent(successEvent));
        assertFalse(storage.addJobExecutionEvent(startEvent));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailureAndConflict() throws SQLException {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent failureEvent = startEvent.executionFailure(new RuntimeException("failure"));
        assertTrue(storage.addJobExecutionEvent(failureEvent));
        assertThat(failureEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
        assertFalse(storage.addJobExecutionEvent(startEvent));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailureAndMessageExceed() throws SQLException {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0);
        assertTrue(storage.addJobExecutionEvent(startEvent));
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            failureMsg.append(i);
        }
        JobExecutionEvent failEvent = startEvent.executionFailure(new RuntimeException("failure" + failureMsg.toString()));
        assertTrue(storage.addJobExecutionEvent(failEvent));
        assertThat(failEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
    
    @Test
    public void assertFindJobExecutionEvent() throws SQLException {
        storage.addJobExecutionEvent(new JobExecutionEvent("fake_task_id", "test_job", ExecutionSource.NORMAL_TRIGGER, 0));
    }
}
