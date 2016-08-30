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

import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;


public class JobEventRdbStorageTest {
    
    private JobEventRdbStorage storage;
    
    @Before
    public void setup() throws SQLException {
        storage = new JobEventRdbStorage(org.h2.Driver.class.getName(), "jdbc:h2:mem:job_event_storage", "sa", "", LogLevel.INFO);
    }
    
    @Test
    public void assertAddJobTraceEvent() throws SQLException {
        assertTrue(storage.addJobTraceEvent(new JobTraceEvent("test_job", LogLevel.WARN, "message")));
    }
    
    @Test
    public void assertAddJobTraceEventWhenFailure() throws SQLException {
        assertFalse(storage.addJobTraceEvent(new JobTraceEvent("test_job", LogLevel.DEBUG, "message", new Exception("failure"))));
    }
    
    @Test
    public void assertAddTraceLogWithLogLevel() throws SQLException {
        JobEventRdbStorage rdbStorage = new JobEventRdbStorage(org.h2.Driver.class.getName(), "jdbc:h2:mem:job_event_storage", "sa", "", LogLevel.INFO);
        assertFalse(rdbStorage.addJobTraceEvent(new JobTraceEvent("test_job", LogLevel.DEBUG, "message")));
    }
    
    @Test
    public void assertAddJobExecutionEvent() throws SQLException {
        assertTrue(storage.addJobExecutionEvent(new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Lists.newArrayList(0))));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenSuccess() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Lists.newArrayList(0));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        jobExecutionEvent.executionSuccess();
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertNotNull(jobExecutionEvent.getCompleteTime());
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailure() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Lists.newArrayList(0));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        jobExecutionEvent.executionFailure(new RuntimeException("failure"));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertThat(jobExecutionEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
    
    @Test
    public void assertUpdateJobExecutionEventWhenFailureAndMessageExceed() throws SQLException {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Lists.newArrayList(0));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < 17000; i++) {
            failureMsg.append(i);
        }
        jobExecutionEvent.executionFailure(new RuntimeException("failure" + failureMsg.toString()));
        assertTrue(storage.addJobExecutionEvent(jobExecutionEvent));
        assertThat(jobExecutionEvent.getFailureCause(), startsWith("java.lang.RuntimeException: failure"));
    }
}
