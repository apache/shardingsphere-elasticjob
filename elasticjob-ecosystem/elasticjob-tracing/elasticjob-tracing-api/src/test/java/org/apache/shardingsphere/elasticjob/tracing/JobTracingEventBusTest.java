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

package org.apache.shardingsphere.elasticjob.tracing;

import com.google.common.eventbus.EventBus;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.event.JobEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.fixture.JobEventCaller;
import org.apache.shardingsphere.elasticjob.tracing.fixture.TestTracingListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobTracingEventBusTest {
    
    @Mock
    private JobEventCaller jobEventCaller;
    
    @Mock
    private EventBus eventBus;
    
    private JobTracingEventBus jobTracingEventBus;
    
    @Test
    public void assertRegisterFailure() {
        jobTracingEventBus = new JobTracingEventBus(new TracingConfiguration<>("FAIL", null));
        assertIsRegistered(false);
    }
    
    @Test
    public void assertPost() throws InterruptedException {
        jobTracingEventBus = new JobTracingEventBus(new TracingConfiguration<>("TEST", jobEventCaller));
        assertIsRegistered(true);
        jobTracingEventBus.post(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_event_bus_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
        while (!TestTracingListener.isExecutionEventCalled()) {
            Thread.sleep(100L);
        }
        verify(jobEventCaller).call();
    }
    
    @Test
    public void assertPostWithoutListener() throws ReflectiveOperationException {
        jobTracingEventBus = new JobTracingEventBus();
        assertIsRegistered(false);
        Field field = JobTracingEventBus.class.getDeclaredField("eventBus");
        field.setAccessible(true);
        field.set(jobTracingEventBus, eventBus);
        jobTracingEventBus.post(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_event_bus_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
        verify(eventBus, times(0)).post(ArgumentMatchers.<JobEvent>any());
    }
    
    @SneakyThrows
    private void assertIsRegistered(final boolean actual) {
        Field field = JobTracingEventBus.class.getDeclaredField("isRegistered");
        field.setAccessible(true);
        assertThat(field.get(jobTracingEventBus), is(actual));
    }
}
