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
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import org.junit.Test;

public final class JobEventLogListenerTest {
    
    private static final String JOB_NAME = "test_log_event_listener_job";
    
    private JobEventBus jobEventBus = new JobEventBus();
    
    @Test
    public void assertPostJobTraceEvent() {
        for (LogLevel each : LogLevel.values()) {
            jobEventBus.post(new JobTraceEvent(JOB_NAME, each, "ok"));
        }
    }
    
    @Test
    public void assertPostJobExecutionEventWhenStart() {
        jobEventBus.post(new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
    }
    
    @Test
    public void assertPostJobExecutionEventWhenCompleteWithSuccess() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobExecutionEvent.executionSuccess();
        jobEventBus.post(jobExecutionEvent);
    }
    
    @Test
    public void assertPostJobExecutionEventWhenCompleteWithFailure() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobExecutionEvent.executionFailure(new RuntimeException("test"));
        jobEventBus.post(jobExecutionEvent);
    }
}
