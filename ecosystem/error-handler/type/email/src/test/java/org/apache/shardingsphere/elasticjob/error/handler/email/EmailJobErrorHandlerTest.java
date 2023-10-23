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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailJobErrorHandlerTest {
    
    private static List<LoggingEvent> appenderList;
    
    @Mock
    private Session session;
    
    @Mock
    private Transport transport;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeAll
    static void init() {
        ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(EmailJobErrorHandler.class);
        ListAppender<LoggingEvent> appender = (ListAppender) log.getAppender("EmailJobErrorHandlerTestAppender");
        appenderList = appender.list;
    }
    
    @BeforeEach
    void setUp() {
        appenderList.clear();
    }
    
    @Test
    void assertHandleExceptionWithMessagingException() {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler(createConfigurationProperties());
        Throwable cause = new RuntimeException("test");
        String jobName = "test_job";
        emailJobErrorHandler.handleException(jobName, cause);
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.ERROR));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job' but failed to send email because of"));
    }
    
    @Test
    @SneakyThrows
    void assertHandleExceptionSucceedInSendingEmail() {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler(createConfigurationProperties());
        setUpMockSession(session);
        setFieldValue(emailJobErrorHandler, "session", session);
        Throwable cause = new RuntimeException("test");
        String jobName = "test_job";
        when(session.getTransport()).thenReturn(transport);
        emailJobErrorHandler.handleException(jobName, cause);
        verify(transport).sendMessage(any(Message.class), any(Address[].class));
        assertThat(appenderList.size(), is(1));
        assertThat(appenderList.get(0).getLevel(), is(Level.INFO));
        assertThat(appenderList.get(0).getFormattedMessage(), is("An exception has occurred in Job 'test_job', an email has been sent successfully."));
    }
    
    private EmailJobErrorHandler getEmailJobErrorHandler(final Properties props) {
        return (EmailJobErrorHandler) TypedSPILoader.getService(JobErrorHandler.class, "EMAIL", props);
    }
    
    private void setUpMockSession(final Session session) {
        Properties props = new Properties();
        setFieldValue(session, "props", props);
        when(session.getProperties()).thenReturn(props);
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
        result.setProperty(EmailPropertiesConstants.SSL_TRUST, "*");
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
