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

package org.apache.shardingsphere.elasticjob.kernel.internal.executor.error.handler;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.kernel.internal.executor.error.handler.general.IgnoreJobErrorHandler;
import org.apache.shardingsphere.elasticjob.kernel.internal.executor.error.handler.general.LogJobErrorHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobErrorHandlerReloaderTest {
    
    @Mock
    private JobErrorHandler jobErrorHandler;
    
    @Test
    void assertInitialize() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
        try (JobErrorHandlerReloader jobErrorHandlerReloader = new JobErrorHandlerReloader(jobConfig)) {
            JobErrorHandler actual = jobErrorHandlerReloader.getJobErrorHandler();
            assertNotNull(actual);
            assertThat(actual.getType(), is("IGNORE"));
            assertTrue(actual instanceof IgnoreJobErrorHandler);
        }
    }
    
    @Test
    void assertReload() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
        try (JobErrorHandlerReloader jobErrorHandlerReloader = new JobErrorHandlerReloader(jobConfig)) {
            when(jobErrorHandler.getType()).thenReturn("mock");
            setField(jobErrorHandlerReloader, "jobErrorHandler", jobErrorHandler);
            setField(jobErrorHandlerReloader, "props", new Properties());
            String newJobErrorHandlerType = "LOG";
            JobConfiguration newJobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType(newJobErrorHandlerType).build();
            jobErrorHandlerReloader.reloadIfNecessary(newJobConfig);
            verify(jobErrorHandler).close();
            JobErrorHandler actual = jobErrorHandlerReloader.getJobErrorHandler();
            assertThat(actual.getType(), is(newJobErrorHandlerType));
            assertTrue(actual instanceof LogJobErrorHandler);
        }
    }
    
    @Test
    void assertUnnecessaryToReload() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
        try (JobErrorHandlerReloader jobErrorHandlerReloader = new JobErrorHandlerReloader(jobConfig)) {
            JobErrorHandler expected = jobErrorHandlerReloader.getJobErrorHandler();
            jobErrorHandlerReloader.reloadIfNecessary(jobConfig);
            JobErrorHandler actual = jobErrorHandlerReloader.getJobErrorHandler();
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertShutdown() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType("IGNORE").build();
        try (JobErrorHandlerReloader jobErrorHandlerReloader = new JobErrorHandlerReloader(jobConfig)) {
            setField(jobErrorHandlerReloader, "jobErrorHandler", jobErrorHandler);
            jobErrorHandlerReloader.close();
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
