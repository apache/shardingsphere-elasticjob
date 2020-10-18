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
import org.apache.shardingsphere.elasticjob.error.handler.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.error.handler.email.config.EmailConfiguration;

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
 * Job error handler for send error message via email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    private Session session;
    
    @Override
    public void handleException(final String jobName, final Properties props, final Throwable cause) {
        EmailConfiguration emailConfig = new EmailConfiguration(props);
        String errorMessage = getErrorMessage(jobName, cause);
        try {
            sendMessage(createMessage(errorMessage, emailConfig), emailConfig);
            log.error("An exception has occurred in Job '{}', Notification to email was successful..", jobName, cause);
        } catch (final MessagingException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}', But failed to send alert by email because of", jobName, cause);
        }
    }
    
    private String getErrorMessage(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final String content, final EmailConfiguration emailConfig) throws MessagingException {
        MimeMessage result = new MimeMessage(Optional.ofNullable(session).orElseGet(() -> createSession(emailConfig)));
        result.setFrom(new InternetAddress(emailConfig.getFrom()));
        result.setSubject(emailConfig.getSubject());
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        String to = emailConfig.getTo();
        if (StringUtils.isNotBlank(to)) {
            String[] tos = to.split(",");
            for (String each : tos) {
                result.addRecipient(Message.RecipientType.TO, new InternetAddress(each));
            }
        }
        if (StringUtils.isNotBlank(emailConfig.getCc())) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(emailConfig.getCc()));
        }
        if (StringUtils.isNotBlank(emailConfig.getBcc())) {
            result.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailConfig.getBcc()));
        }
        result.saveChanges();
        return result;
    }
    
    private void sendMessage(final Message message, final EmailConfiguration emailConfig) throws MessagingException {
        try (Transport transport = Optional.ofNullable(session).orElseGet(() -> createSession(emailConfig)).getTransport()) {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
    
    private synchronized Session createSession(final EmailConfiguration emailConfig) {
        if (null == session) {
            session = Session.getDefaultInstance(createSessionProperties(emailConfig), getSessionAuthenticator(emailConfig));
        }
        return session;
    }
    
    private Properties createSessionProperties(final EmailConfiguration emailConfig) {
        Properties result = new Properties();
        result.put("mail.smtp.host", emailConfig.getHost());
        result.put("mail.smtp.port", emailConfig.getPort());
        result.put("mail.smtp.auth", "true");
        result.put("mail.transport.protocol", "smtp");
        result.setProperty("mail.debug", Boolean.toString(emailConfig.isDebug()));
        if (emailConfig.isUseSsl()) {
            result.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            result.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        return result;
    }
    
    private Authenticator getSessionAuthenticator(final EmailConfiguration emailConfig) {
        return new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
            }
        };
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
