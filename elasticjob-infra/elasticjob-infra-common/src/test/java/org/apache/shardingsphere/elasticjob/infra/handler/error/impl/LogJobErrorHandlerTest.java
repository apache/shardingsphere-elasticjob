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

package org.apache.shardingsphere.elasticjob.infra.handler.error.impl;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class LogJobErrorHandlerTest {
    
    @Mock
    private Logger log;
    
    @Test
    public void assertHandleException() {
        LogJobErrorHandler actual = new LogJobErrorHandler();
        setStaticFieldValue(actual);
        Throwable cause = new RuntimeException("test");
        actual.handleException("test_job", cause);
        verify(log).error("Job 'test_job' exception occur in job processing", cause);
    }
    
    @SneakyThrows
    private void setStaticFieldValue(final LogJobErrorHandler logJobErrorHandler) {
        Field field = logJobErrorHandler.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(logJobErrorHandler, log);
    }
}
