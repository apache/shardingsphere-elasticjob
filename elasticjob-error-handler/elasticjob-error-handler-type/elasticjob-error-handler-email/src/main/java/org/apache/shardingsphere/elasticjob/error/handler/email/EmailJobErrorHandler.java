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
import java.util.Optional;
import java.util.Properties;

/**
 * Job error handler for sending error message by email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    private Session session;
    
    private synchronized Session createSession(final EmailConfiguration emailConfiguration) {
        if (null == session) {
            session = Session.getDefaultInstance(createSessionProperties(emailConfiguration), getSessionAuthenticator(emailConfiguration));
        }
        return session;
    }
    
    private Properties createSessionProperties(final EmailConfiguration emailConfiguration) {
        Properties result = new Properties();
        result.put("mail.smtp.host", emailConfiguration.getHost());
        result.put("mail.smtp.port", emailConfiguration.getPort());
        result.put("mail.smtp.auth", "true");
        result.put("mail.transport.protocol", "smtp");
        result.setProperty("mail.debug", Boolean.toString(emailConfiguration.isDebug()));
        if (emailConfiguration.isUseSsl()) {
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
        EmailConfiguration emailConfiguration = EmailConfiguration.getByProps(jobConfig.getProps());
        String errorContext = createErrorContext(jobConfig.getJobName(), cause);
        try {
            sendMessage(createMessage(errorContext, emailConfiguration), emailConfiguration);
            log.error("An exception has occurred in Job '{}', Notification to email was successful..", jobConfig.getJobName(), cause);
        } catch (final MessagingException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}', But failed to send alert by email because of", jobConfig.getJobName(), cause);
        }
    }
    
    private String createErrorContext(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final String content, final EmailConfiguration emailConfiguration) throws MessagingException {
        MimeMessage result = new MimeMessage(Optional.ofNullable(session).orElseGet(() -> createSession(emailConfiguration)));
        result.setFrom(new InternetAddress(emailConfiguration.getFrom()));
        result.setSubject(emailConfiguration.getSubject());
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        String to = emailConfiguration.getTo();
        if (StringUtils.isNotBlank(to)) {
            String[] tos = to.split(",");
            for (String t : tos) {
                result.addRecipient(Message.RecipientType.TO, new InternetAddress(t));
            }
        }
        if (StringUtils.isNotBlank(emailConfiguration.getCc())) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(emailConfiguration.getCc()));
        }
        if (StringUtils.isNotBlank(emailConfiguration.getBcc())) {
            result.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailConfiguration.getBcc()));
        }
        result.saveChanges();
        return result;
    }
    
    private void sendMessage(final Message message, final EmailConfiguration emailConfiguration) throws MessagingException {
        try (Transport transport = Optional.ofNullable(session).orElseGet(() -> createSession(emailConfiguration)).getTransport()) {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
