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

package org.apache.shardingsphere.elasticjob.error.handler.email;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class EmailJobErrorHandlerTest {
    
    @Mock
    private Logger log;
    
    @Test
    public void assertHandleExceptionFor() {
        EmailJobErrorHandler emailJobErrorHandler = new EmailJobErrorHandler();
        emailJobErrorHandler.handleException("test job name", new RuntimeException("test exception"));
    }
    
    @Test
    @SneakyThrows
    public void assertHandleExceptionForNullConfiguration() {
        EmailJobErrorHandler emailJobErrorHandler = new EmailJobErrorHandler();
        Field emailConfigurationField = EmailJobErrorHandler.class.getDeclaredField("emailConfiguration");
        emailConfigurationField.setAccessible(true);
        emailConfigurationField.set(emailJobErrorHandler, null);
        
        setStaticFieldValue(emailJobErrorHandler);
        
        Throwable cause = new RuntimeException("test exception");
        emailJobErrorHandler.handleException("test job name", cause);
        verify(log).error(ArgumentMatchers.any(String.class), ArgumentMatchers.any(NullPointerException.class));
    }
    
    @SneakyThrows
    private void setStaticFieldValue(final EmailJobErrorHandler emailJobErrorHandler) {
        Field field = emailJobErrorHandler.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(emailJobErrorHandler, log);
    }
    
    @Test
    public void assertType() {
        EmailJobErrorHandler emailJobErrorHandler = new EmailJobErrorHandler();
        assertThat(emailJobErrorHandler.getType(), equalTo("EMAIL"));
    }
}
