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
import com.dangdang.ddframe.job.event.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public final class JobLogEventListenerTest {
    
    private JobEventConfiguration logEventConfig = new JobLogEventConfiguration();
    
    private JobEventBus jobEventBus = JobEventBus.getInstance();
    
    @Before
    public void setUp() {
        jobEventBus.register(new JobEventConfiguration[]{logEventConfig});
    }
    
    @After
    public void tearDown() {
        jobEventBus.clearListeners();
    }
    
    @Test
    public void assertPostWithJobTraceEvent() {
        for (LogLevel each : LogLevel.values()) {
            jobEventBus.post(new JobTraceEvent("test_job", each, "ok"));
        }
    }
    
    @Test
    public void assertPostWithJobExecutionEventWhenExecutionSuccess() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Arrays.asList(0, 1));
        jobEventBus.post(jobExecutionEvent);
        jobExecutionEvent.executionSuccess();
        jobEventBus.post(jobExecutionEvent);
    }
    
    @Test
    public void assertPostWithJobTraceEventWhenExecutionFailure() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Arrays.asList(0, 1));
        jobEventBus.post(jobExecutionEvent);
        jobExecutionEvent.executionFailure(new Exception("Failure"));
        jobEventBus.post(jobExecutionEvent);
    }
}
