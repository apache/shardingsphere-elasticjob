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

package org.apache.shardingsphere.elasticjob.infra.concurrent;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.JobExecutorServiceHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutorServiceReloadableTest {
    
    @Mock
    private ExecutorService mockExecutorService;
    
    @Test
    void assertInitialize() {
        ExecutorServiceReloadable executorServiceReloadable = new ExecutorServiceReloadable();
        String jobExecutorServiceHandlerType = "SINGLE_THREAD";
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobExecutorServiceHandlerType(jobExecutorServiceHandlerType).build();
        assertNull(executorServiceReloadable.getInstance());
        executorServiceReloadable.init(jobConfig);
        ExecutorService actual = executorServiceReloadable.getInstance();
        assertNotNull(actual);
        assertFalse(actual.isShutdown());
        assertFalse(actual.isTerminated());
        actual.shutdown();
    }
    
    @Test
    void assertReload() {
        ExecutorServiceReloadable executorServiceReloadable = new ExecutorServiceReloadable();
        JobExecutorServiceHandler jobExecutorServiceHandler = mock(JobExecutorServiceHandler.class);
        when(jobExecutorServiceHandler.getType()).thenReturn("mock");
        setField(executorServiceReloadable, "jobExecutorServiceHandler", jobExecutorServiceHandler);
        setField(executorServiceReloadable, "executorService", mockExecutorService);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).build();
        executorServiceReloadable.reloadIfNecessary(jobConfig);
        verify(mockExecutorService).shutdown();
        ExecutorService actual = executorServiceReloadable.getInstance();
        assertFalse(actual.isShutdown());
        assertFalse(actual.isTerminated());
        actual.shutdown();
    }
    
    @Test
    void assertUnnecessaryToReload() {
        ExecutorServiceReloadable executorServiceReloadable = new ExecutorServiceReloadable();
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobExecutorServiceHandlerType("CPU").build();
        executorServiceReloadable.init(jobConfig);
        ExecutorService expected = executorServiceReloadable.getInstance();
        executorServiceReloadable.reloadIfNecessary(jobConfig);
        ExecutorService actual = executorServiceReloadable.getInstance();
        assertThat(actual, is(expected));
        actual.shutdown();
    }
    
    @Test
    void assertShutdown() {
        ExecutorServiceReloadable executorServiceReloadable = new ExecutorServiceReloadable();
        setField(executorServiceReloadable, "executorService", mockExecutorService);
        executorServiceReloadable.close();
        verify(mockExecutorService).shutdown();
    }
    
    @SneakyThrows
    private void setField(final Object target, final String fieldName, final Object value) {
        Field field = target.getClass().getDeclaredField(fieldName);
        boolean originAccessible = field.isAccessible();
        if (!originAccessible) {
            field.setAccessible(true);
        }
        field.set(target, value);
        field.setAccessible(originAccessible);
    }
}
