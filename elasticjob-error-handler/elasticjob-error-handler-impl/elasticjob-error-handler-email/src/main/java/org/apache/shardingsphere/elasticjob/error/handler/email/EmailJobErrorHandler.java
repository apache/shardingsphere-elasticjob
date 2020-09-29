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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

/**
 * Job error handler for sending error message by email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    private Session session;
    
    private Properties createSessionProperties(final EmailConfiguration emailConfiguration) {
        Properties result = new Properties();
        result.put("mail.smtp.host", emailConfiguration.getHost());
        result.put("mail.smtp.port", emailConfiguration.getPort());
        result.put("mail.smtp.auth", "true");
        result.put("mail.transport.protocol", emailConfiguration.getProtocol());
        result.setProperty("mail.debug", emailConfiguration.getDebug());
        if (Strings.isNullOrEmpty(emailConfiguration.getUseSsl()) && Boolean.valueOf(emailConfiguration.getUseSsl())) {
            result.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            result.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        return result;
    }
    
    private Authenticator getSessionAuthenticator(final EmailConfiguration emailConfiguration) {
        return new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfiguration.getUsername(), emailConfiguration.getPassword());
            }
        };
    }
    
    @Override
    public void handleException(final JobConfiguration jobConfig, final Throwable cause) {
        try {
            if (session == null) {
                EmailConfiguration emailConfiguration = EmailConfiguration.getByProps(jobConfig.getProps());
                session = Session.getDefaultInstance(createSessionProperties(emailConfiguration), getSessionAuthenticator(emailConfiguration));
            }
            sendMessage(createMessage(jobConfig, cause));
        } catch (final MessagingException ex) {
            log.error("Elastic job: email job handler error", ex);
        }
    }
    
    private String createErrorContext(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final JobConfiguration jobConfig, final Throwable cause) throws MessagingException {
        EmailConfiguration emailConfiguration = EmailConfiguration.getByProps(jobConfig.getProps());
        MimeMessage result = new MimeMessage(session);
        result.setFrom(new InternetAddress(emailConfiguration.getFrom()));
        result.setSubject(emailConfiguration.getSubject());
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(createErrorContext(jobConfig.getJobName(), cause), "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        if (StringUtils.isNotBlank(emailConfiguration.getTo())) {
            result.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfiguration.getTo()));
        }
        if (StringUtils.isNotBlank(emailConfiguration.getCc())) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(emailConfiguration.getCc()));
        }
        result.saveChanges();
        return result;
    }
    
    private void sendMessage(final Message message) throws MessagingException {
        try (Transport transport = session.getTransport()) {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
