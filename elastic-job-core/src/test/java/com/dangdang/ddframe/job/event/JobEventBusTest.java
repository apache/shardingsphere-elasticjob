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

package com.dangdang.ddframe.job.event;

import com.dangdang.ddframe.job.event.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import com.dangdang.ddframe.job.event.fixture.Caller;
import com.dangdang.ddframe.job.event.fixture.TestJobEventListener;
import com.dangdang.ddframe.job.event.fixture.TestJobEventConfiguration;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobEventBusTest {
    
    @Mock
    private Caller caller;
    
    private final JobEventBus jobEventBus = JobEventBus.getInstance();
    
    private final String jobName = "test_event_bus_job";
    
    @After
    public void tearDown() {
        jobEventBus.clearListeners(jobName);
        TestJobEventListener.reset();
    }
    
    @Test
    public void assertPostWithoutListenerRegistered() {
        jobEventBus.post(jobName, new JobTraceEvent("test_job", LogLevel.INFO, "ok"));
        jobEventBus.post(jobName, new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Arrays.asList(0, 1)));
        verify(caller, times(0)).call();
    }
    
    @Test
    public void assertPostWithListenerRegistered() throws InterruptedException {
        registerEventConfigs();
        jobEventBus.post(jobName, new JobTraceEvent("test_job", LogLevel.INFO, "ok"));
        jobEventBus.post(jobName, new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Arrays.asList(0, 1)));
        while (!TestJobEventListener.isExecutionEventCalled() || !TestJobEventListener.isTraceEventCalled()) {
            Thread.sleep(100L);
        }
        verify(caller, times(2)).call();
    }
    
    @Test
    public void assertPostWithListenerRegisteredTwice() throws InterruptedException {
        registerEventConfigs();
        registerEventConfigs();
        jobEventBus.post(jobName, new JobTraceEvent("test_job", LogLevel.INFO, "ok"));
        jobEventBus.post(jobName, new JobExecutionEvent("test_job", ExecutionSource.NORMAL_TRIGGER, Arrays.asList(0, 1)));
        while (!TestJobEventListener.isExecutionEventCalled() || !TestJobEventListener.isTraceEventCalled()) {
            Thread.sleep(100L);
        }
        verify(caller, times(2)).call();
    }
    
    private void registerEventConfigs() {
        Map<String, JobEventConfiguration> jobEventConfigs = new LinkedHashMap<>(1, 1);
        TestJobEventConfiguration jobEventConfiguration = new TestJobEventConfiguration(caller);
        jobEventConfigs.put("test", jobEventConfiguration);
        jobEventBus.register(jobName, jobEventConfigs.values());
    }
}
