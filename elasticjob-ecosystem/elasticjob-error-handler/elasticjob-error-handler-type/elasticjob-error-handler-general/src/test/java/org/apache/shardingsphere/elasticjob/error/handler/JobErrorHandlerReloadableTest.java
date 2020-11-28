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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class JobErrorHandlerReloadableTest {
    
    @Mock
    private JobErrorHandler mockJobErrorHandler;
    
    @Test
    public void assertInitialize() {
        JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable();
        String jobErrorHandlerType = "IGNORE";
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType(jobErrorHandlerType).build();
        assertNull(jobErrorHandlerReloadable.getInstance());
        jobErrorHandlerReloadable.init(jobConfig);
        JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
        assertNotNull(actual);
        assertThat(actual.getType(), equalTo(jobErrorHandlerType));
        assertTrue(actual instanceof IgnoreJobErrorHandler);
    }
    
    @Test
    public void assertReload() {
        JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable();
        setField(jobErrorHandlerReloadable, "jobErrorHandler", mockJobErrorHandler);
        setField(jobErrorHandlerReloadable, "jobErrorHandlerType", "mock");
        setField(jobErrorHandlerReloadable, "props", new Properties());
        String newJobErrorHandlerType = "LOG";
        JobConfiguration newJobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType(newJobErrorHandlerType).build();
        jobErrorHandlerReloadable.reloadIfNecessary(newJobConfig);
        verify(mockJobErrorHandler).close();
        JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
        assertThat(actual.getType(), equalTo(newJobErrorHandlerType));
        assertTrue(actual instanceof LogJobErrorHandler);
    }
    
    @Test
    public void assertUnnecessaryToReload() {
        JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable();
        String jobErrorHandlerType = "IGNORE";
        JobConfiguration jobConfig = JobConfiguration.newBuilder("job", 1).jobErrorHandlerType(jobErrorHandlerType).build();
        jobErrorHandlerReloadable.init(jobConfig);
        JobErrorHandler expected = jobErrorHandlerReloadable.getInstance();
        jobErrorHandlerReloadable.reloadIfNecessary(jobConfig);
        JobErrorHandler actual = jobErrorHandlerReloadable.getInstance();
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertShutdown() {
        JobErrorHandlerReloadable jobErrorHandlerReloadable = new JobErrorHandlerReloadable();
        setField(jobErrorHandlerReloadable, "jobErrorHandler", mockJobErrorHandler);
        jobErrorHandlerReloadable.close();
        verify(mockJobErrorHandler).close();
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
