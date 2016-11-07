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

import com.dangdang.ddframe.job.event.fixture.JobEventCaller;
import com.dangdang.ddframe.job.event.fixture.TestJobEventConfiguration;
import com.dangdang.ddframe.job.event.fixture.TestJobEventListener;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent.ExecutionSource;
import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobEventBusTest {
    
    @Mock
    private JobEventCaller jobEventCaller;
    
    private JobEventBus jobEventBus;
    
    @Before
    public void setUp() {
        jobEventBus = new JobEventBus(new TestJobEventConfiguration(jobEventCaller));
    }
    
    @Test
    public void assertPost() throws InterruptedException {
        jobEventBus.post(new JobTraceEvent("test_event_bus_job", LogLevel.INFO, "ok"));
        jobEventBus.post(new JobExecutionEvent("test_event_bus_job", ExecutionSource.NORMAL_TRIGGER, 0));
        while (!TestJobEventListener.isExecutionEventCalled() || !TestJobEventListener.isTraceEventCalled()) {
            Thread.sleep(100L);
        }
        verify(jobEventCaller, times(2)).call();
    }
    
    // TODO
//    @Test
//    public void assertGetWorkQueueSize() {
//        registerEventConfigs();
//        assertThat(jobEventBus.getWorkQueueSize().size(), is(1));
//        assertThat(jobEventBus.getWorkQueueSize().get(jobName), is(0));
//    }
}
