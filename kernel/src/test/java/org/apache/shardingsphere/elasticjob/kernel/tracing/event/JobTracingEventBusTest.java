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

package org.apache.shardingsphere.elasticjob.kernel.tracing.event;

import com.google.common.eventbus.EventBus;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.fixture.config.TracingStorageFixture;
import org.apache.shardingsphere.elasticjob.kernel.tracing.fixture.listener.TracingListenerFixture;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobTracingEventBusTest {
    
    @Mock
    private TracingStorageFixture tracingStorage;
    
    @Mock
    private EventBus eventBus;
    
    private JobTracingEventBus jobTracingEventBus;
    
    @Test
    void assertRegisterWithoutTracingStorageConfiguration() {
        jobTracingEventBus = new JobTracingEventBus(new TracingConfiguration<>("TEST", null));
        assertFalse((Boolean) ReflectionUtils.getFieldValue(jobTracingEventBus, "isRegistered"));
    }
    
    @Test
    void assertPost() {
        jobTracingEventBus = new JobTracingEventBus(new TracingConfiguration<>("TEST", tracingStorage));
        assertTrue((Boolean) ReflectionUtils.getFieldValue(jobTracingEventBus, "isRegistered"));
        jobTracingEventBus.post(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_event_bus_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
        Awaitility.await().pollDelay(100L, TimeUnit.MILLISECONDS).until(TracingListenerFixture::isExecutionEventCalled);
        verify(tracingStorage).call();
    }
    
    @Test
    void assertPostWithoutListener() {
        jobTracingEventBus = new JobTracingEventBus();
        assertFalse((Boolean) ReflectionUtils.getFieldValue(jobTracingEventBus, "isRegistered"));
        ReflectionUtils.setFieldValue(jobTracingEventBus, "eventBus", eventBus);
        jobTracingEventBus.post(new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_event_bus_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0));
        verify(eventBus, times(0)).post(ArgumentMatchers.<JobEvent>any());
    }
}
