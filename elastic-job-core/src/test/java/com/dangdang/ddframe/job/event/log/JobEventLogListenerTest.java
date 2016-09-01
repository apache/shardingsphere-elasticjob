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

package com.dangdang.ddframe.job.event.log;

import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

public final class JobEventLogListenerTest {
    
    private static final String JOB_NAME = "test_log_event_listener_job";
    
    @BeforeClass
    public static void setUp() {
        JobEventBus.getInstance().register(JOB_NAME, Collections.<JobEventConfiguration>singletonList(new JobEventLogConfiguration()));
    }
    
    @AfterClass
    public static void tearDown() {
        JobEventBus.getInstance().clearListeners(JOB_NAME);
    }
    
    @Test
    public void assertPostJobTraceEvent() {
        for (LogLevel each : LogLevel.values()) {
            JobEventBus.getInstance().post(JOB_NAME, new JobTraceEvent(JOB_NAME, each, "ok"));
        }
    }
    
    @Test
    public void assertPostJobExecutionEventWhenStart() {
        JobEventBus.getInstance().post(JOB_NAME, new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
    }
    
    @Test
    public void assertPostJobExecutionEventWhenCompleteWithSuccess() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobExecutionEvent.executionSuccess();
        JobEventBus.getInstance().post(JOB_NAME, jobExecutionEvent);
    }
    
    @Test
    public void assertPostJobExecutionEventWhenCompleteWithFailure() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobExecutionEvent.executionFailure(new RuntimeException("test"));
        JobEventBus.getInstance().post(JOB_NAME, jobExecutionEvent);
    }
}
