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
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class EmailJobErrorHandlerTest {
    
    @Mock
    private Logger log;
    
    @Test
    public void assertHandleExceptionWithMessagingException() {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler();
        setStaticFieldValue(emailJobErrorHandler, "log", log);
        Throwable cause = new RuntimeException("test");
        emailJobErrorHandler.handleException(getJobConfiguration(), cause);
        verify(log).error("An exception has occurred in Job '{}', But failed to send alert by email because of", "test_job", cause);
    }
    
    @SneakyThrows
    private void setStaticFieldValue(final EmailJobErrorHandler wechatJobErrorHandler, final String name, final Object value) {
        Field fieldLog = wechatJobErrorHandler.getClass().getDeclaredField(name);
        fieldLog.setAccessible(true);
        Field modifiers = fieldLog.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(fieldLog, fieldLog.getModifiers() & ~Modifier.FINAL);
        fieldLog.set(wechatJobErrorHandler, value);
    }
    
    private JobConfiguration getJobConfiguration() {
        return JobConfiguration.newBuilder("test_job", 3)
                .setProperty(EmailConstants.EMAIL_HOST, "xxx")
                .setProperty(EmailConstants.EMAIL_PORT, "465")
                .setProperty(EmailConstants.EMAIL_USERNAME, "xxx")
                .setProperty(EmailConstants.EMAIL_PASSWORD, "xxx")
                .setProperty(EmailConstants.EMAIL_PROTOCOL, "smtp")
                .setProperty(EmailConstants.EMAIL_USE_SSL, "true")
                .setProperty(EmailConstants.EMAIL_SUBJECT, "Unit test notification")
                .setProperty(EmailConstants.EMAIL_FROM, "from@xxx.com")
                .setProperty(EmailConstants.EMAIL_TO, "to1@xxx.com,to2@xxx.com")
                .setProperty(EmailConstants.EMAIL_CC, "cc@xxx.com")
                .setProperty(EmailConstants.EMAIL_BCC, "bcc@xxx.com")
                .setProperty(EmailConstants.EMAIL_DEBUG, "false")
                .build();
    }
    
    private EmailJobErrorHandler getEmailJobErrorHandler() {
        return (EmailJobErrorHandler) JobErrorHandlerFactory.createHandler("EMAIL").orElseThrow(() -> new JobConfigurationException("EMAIL error handler not found."));
    }
}
