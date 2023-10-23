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

package org.apache.shardingsphere.elasticjob.error.handler;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.general.IgnoreJobErrorHandler;
import org.apache.shardingsphere.elasticjob.error.handler.general.LogJobErrorHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobErrorHandlerReloadableTest {
    
    @Mock
    private JobErrorHandler jobErrorHandler;
    
    @Test
    void assertInitialize() {
        try (JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable()) {
            JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
            assertNull(jobErrorHandlerReloadable.getInstance());
            jobErrorHandlerReloadable.init(jobConfig);
            JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
            assertNotNull(actual);
            assertThat(actual.getType(), is("IGNORE"));
            assertTrue(actual instanceof IgnoreJobErrorHandler);
        }
    }
    
    @Test
    void assertReload() {
        try (JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable()) {
            when(jobErrorHandler.getType()).thenReturn("mock");
            setField(jobErrorHandlerReloadable, "jobErrorHandler", jobErrorHandler);
            setField(jobErrorHandlerReloadable, "props", new Properties());
            String newJobErrorHandlerType = "LOG";
            JobConfiguration newJobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType(newJobErrorHandlerType).build();
            jobErrorHandlerReloadable.reloadIfNecessary(newJobConfig);
            verify(jobErrorHandler).close();
            JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
            assertThat(actual.getType(), is(newJobErrorHandlerType));
            assertTrue(actual instanceof LogJobErrorHandler);
        }
    }
    
    @Test
    void assertUnnecessaryToReload() {
        try (JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable()) {
            JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
            jobErrorHandlerReloadable.init(jobConfig);
            JobErrorHandler expected = jobErrorHandlerReloadable.getInstance();
            jobErrorHandlerReloadable.reloadIfNecessary(jobConfig);
            JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertShutdown() {
        try (JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable()) {
            setField(jobErrorHandlerReloadable, "jobErrorHandler", jobErrorHandler);
            jobErrorHandlerReloadable.close();
            verify(jobErrorHandler).close();
        }
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
