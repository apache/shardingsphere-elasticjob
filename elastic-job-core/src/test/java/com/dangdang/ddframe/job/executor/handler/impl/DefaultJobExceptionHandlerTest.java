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

package com.dangdang.ddframe.job.executor.handler.impl;

import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.fixture.JobEventCaller;
import com.dangdang.ddframe.job.event.fixture.TestJobEventConfiguration;
import com.dangdang.ddframe.job.event.fixture.TestJobEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultJobExceptionHandlerTest {
    
    @Mock
    private JobEventCaller caller;
    
    @Before
    public void setUp() {
        JobEventBus.getInstance().register("test_job", Collections.<JobEventConfiguration>singletonList(new TestJobEventConfiguration(caller)));
    }
    
    @After
    public void tearDown() {
        JobEventBus.getInstance().clearListeners("test_job");
        TestJobEventListener.reset();
    }
    
    @Test
    public void assertHandleException() {
        new DefaultJobExceptionHandler().handleException("test_job", new RuntimeException("test"));
        verify(caller).call();
    }
}
