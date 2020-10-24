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
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandlerFactory;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EmailJobErrorHandlerTest {
    
    @Mock
    private Logger log;
    
    @Mock
    private Session session;
    
    @Mock
    private Transport transport;
    
    @Test
    public void assertHandleExceptionWithMessagingException() {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler(createConfigurationProperties());
        setStaticFieldValue(emailJobErrorHandler, "log", log);
        Throwable cause = new RuntimeException("test");
        String jobName = "test_job";
        emailJobErrorHandler.handleException(jobName, cause);
        verify(log).error("An exception has occurred in Job '{}' but failed to send email because of", jobName, cause);
    }
    
    @Test
    @SneakyThrows
    public void assertHandleExceptionSucceedInSendingEmail() {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler(createConfigurationProperties());
        setStaticFieldValue(emailJobErrorHandler, "log", log);
        setUpMockSession(session);
        setFieldValue(emailJobErrorHandler, "session", session);
        Throwable cause = new RuntimeException("test");
        String jobName = "test_job";
        when(session.getTransport()).thenReturn(transport);
        emailJobErrorHandler.handleException(jobName, cause);
        verify(transport).sendMessage(any(Message.class), any(Address[].class));
        verify(log).info("An exception has occurred in Job '{}', an email has been sent successfully.", jobName, cause);
    }
    
    private EmailJobErrorHandler getEmailJobErrorHandler(final Properties props) {
        return (EmailJobErrorHandler) JobErrorHandlerFactory.createHandler("EMAIL", props).orElseThrow(() -> new JobConfigurationException("EMAIL error handler not found."));
    }
    
    private void setUpMockSession(final Session session) {
        Properties props = new Properties();
        setFieldValue(session, "props", props);
        when(session.getProperties()).thenReturn(props);
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
    
    @SneakyThrows
    private void setFieldValue(final Object target, final String fieldName, final Object fieldValue) {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
    
    private Properties createConfigurationProperties() {
        Properties result = new Properties();
        result.setProperty(EmailPropertiesConstants.HOST, "localhost");
        result.setProperty(EmailPropertiesConstants.PORT, "465");
        result.setProperty(EmailPropertiesConstants.USERNAME, "user");
        result.setProperty(EmailPropertiesConstants.PASSWORD, "xxx");
        result.setProperty(EmailPropertiesConstants.SUBJECT, "Unit test notification");
        result.setProperty(EmailPropertiesConstants.FROM, "from@xxx.xx");
        result.setProperty(EmailPropertiesConstants.TO, "to1@xxx.xx,to2@xxx.xx");
        result.setProperty(EmailPropertiesConstants.CC, "cc@xxx.xx");
        result.setProperty(EmailPropertiesConstants.BCC, "bcc@xxx.xx");
        return result;
    }
}
