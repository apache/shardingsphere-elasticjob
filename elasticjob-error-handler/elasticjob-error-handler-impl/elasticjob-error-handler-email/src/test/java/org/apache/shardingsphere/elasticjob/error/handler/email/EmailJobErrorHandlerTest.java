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

import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.mail.Session;
import java.lang.reflect.Field;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class EmailJobErrorHandlerTest {
    
    @Test
    public void assertHandleExceptionWithYAMLConfiguration() throws ReflectiveOperationException {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler();
        emailJobErrorHandler.handleException("test job name", new RuntimeException("test exception"));
        Field field = emailJobErrorHandler.getClass().getDeclaredField("config");
        field.setAccessible(true);
        EmailConfiguration config = (EmailConfiguration) field.get(emailJobErrorHandler);
        assertNotNull(config);
        assertThat(config.getHost(), equalTo("yaml.email.com"));
        assertThat(config.getPort(), equalTo(123));
        assertThat(config.getUsername(), equalTo("yaml.username"));
        assertThat(config.getFrom(), equalTo("yaml.from@ejob.com"));
        assertThat(config.getTo(), equalTo("yaml.to@ejob.com"));
        assertThat(config.getBcc(), equalTo("yaml.bcc@ejob.com"));
        assertThat(config.getProtocol(), equalTo("yaml.smtp"));
        assertThat(config.getSubject(), equalTo("yaml.subject"));
        assertTrue(config.isUseSsl());
        assertTrue(config.isDebug());
    }
    
    @Test
    public void assertHandleExceptionWithSession() throws ReflectiveOperationException {
        EmailJobErrorHandler emailJobErrorHandler = getEmailJobErrorHandler();
        emailJobErrorHandler.handleException("test job name", new RuntimeException("test exception"));
        Field field = emailJobErrorHandler.getClass().getDeclaredField("session");
        field.setAccessible(true);
        Session session = (Session) field.get(emailJobErrorHandler);
        assertNotNull(session);
        assertThat(session.getProperties().get("mail.smtp.host"), equalTo("yaml.email.com"));
        assertThat(session.getProperties().get("mail.debug"), equalTo("true"));
        assertThat(session.getProperties().get("mail.smtp.port"), equalTo(123));
        assertThat(session.getProperties().get("mail.transport.protocol"), equalTo("yaml.smtp"));
        assertThat(session.getProperties().get("mail.smtp.auth"), equalTo("true"));
    }
    
    private EmailJobErrorHandler getEmailJobErrorHandler() {
        for (JobErrorHandler each : ServiceLoader.load(JobErrorHandler.class)) {
            if (null != each && each instanceof EmailJobErrorHandler) {
                return (EmailJobErrorHandler) each;
            }
        }
        return new EmailJobErrorHandler();
    }
}
