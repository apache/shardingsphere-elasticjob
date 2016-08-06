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

package com.dangdang.ddframe.job.util.event;

import com.dangdang.ddframe.job.util.event.fixture.Caller;
import com.dangdang.ddframe.job.util.event.fixture.TestTraceEvenListener;
import com.dangdang.ddframe.job.util.trace.TraceEvent;
import com.dangdang.ddframe.job.util.trace.TraceEventBus;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TraceEventBusTest {
    
    @Mock
    private Caller caller;
    
    private TraceEventBus traceEventBus = TraceEventBus.getInstance();
    
    @After
    public void tearDown() {
        traceEventBus.clearListeners();
    }
    
    @Test
    public void assertPostWithoutListenerRegistered() {
        traceEventBus.post(new TraceEvent("test_job", TraceEvent.Level.INFO, "ok"));
        verify(caller, times(0)).call();
    }
    
    @Test
    public void assertPostWithListenerRegistered() {
        traceEventBus.register(new TestTraceEvenListener(caller));
        traceEventBus.post(new TraceEvent("test_job", TraceEvent.Level.INFO, "ok"));
        verify(caller).call();
    }
    
    @Test
    public void assertPostWithListenerRegisteredTwice() {
        traceEventBus.register(new TestTraceEvenListener(caller));
        traceEventBus.register(new TestTraceEvenListener(caller));
        traceEventBus.post(new TraceEvent("test_job", TraceEvent.Level.INFO, "ok"));
        verify(caller).call();
    }
}
