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
import com.dangdang.ddframe.job.event.fixture.TestJobEventFailureConfiguration;
import com.dangdang.ddframe.job.event.fixture.TestJobEventListener;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent.ExecutionSource;
import com.google.common.eventbus.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobEventBusTest {
    
    @Mock
    private JobEventCaller jobEventCaller;
    
    @Mock
    private EventBus eventBus;
    
    private JobEventBus jobEventBus;
    
    @Test
    public void assertRegisterFailure() throws NoSuchFieldException {
        jobEventBus = new JobEventBus(new TestJobEventFailureConfiguration());
        assertIsRegistered(false);
    }
    
    @Test
    public void assertPost() throws InterruptedException, NoSuchFieldException {
        jobEventBus = new JobEventBus(new TestJobEventConfiguration(jobEventCaller));
        assertIsRegistered(true);
        jobEventBus.post(new JobExecutionEvent("fake_task_id", "test_event_bus_job", ExecutionSource.NORMAL_TRIGGER, 0));
        while (!TestJobEventListener.isExecutionEventCalled()) {
            Thread.sleep(100L);
        }
        verify(jobEventCaller).call();
    }
    
    @Test
    public void assertPostWithoutListener() throws NoSuchFieldException {
        jobEventBus = new JobEventBus();
        assertIsRegistered(false);
        ReflectionUtils.setFieldValue(jobEventBus, "eventBus", eventBus);
        jobEventBus.post(new JobExecutionEvent("fake_task_id", "test_event_bus_job", ExecutionSource.NORMAL_TRIGGER, 0));
        verify(eventBus, times(0)).post(ArgumentMatchers.<JobEvent>any());
    }
    
    private void assertIsRegistered(final boolean actual) throws NoSuchFieldException {
        assertThat((boolean) ReflectionUtils.getFieldValue(jobEventBus, JobEventBus.class.getDeclaredField("isRegistered")), is(actual));
    }
}
