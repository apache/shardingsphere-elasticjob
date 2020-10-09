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
import java.util.Properties;

/**
 * Job error handler for sending error message by email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    public static final String CONFIG_PREFIX = "email";
    
    private final EmailConfiguration config;
    
    private final Session session;
    
    public EmailJobErrorHandler() {
        config = EmailConfigurationLoader.unmarshal(CONFIG_PREFIX);
        session = Session.getDefaultInstance(createSessionProperties(), getSessionAuthenticator());
    }
    
    private Properties createSessionProperties() {
        Properties result = new Properties();
        result.put("mail.smtp.host", config.getHost());
        result.put("mail.smtp.port", config.getPort());
        result.put("mail.smtp.auth", "true");
        result.put("mail.transport.protocol", config.getProtocol());
        result.setProperty("mail.debug", Boolean.toString(config.isDebug()));
        if (config.isUseSsl()) {
            result.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            result.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        return result;
    }
    
    private Authenticator getSessionAuthenticator() {
        return new Authenticator() {
            
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        };
    }
    
    @Override
    public void handleException(final JobConfiguration jobConfiguration, final Throwable cause) {
        String errorContext = createErrorContext(jobConfiguration.getJobName(), cause);
        try {
            sendMessage(createMessage(errorContext));
        } catch (final MessagingException ex) {
            log.error("Elastic job: email job handler error", ex);
        }
    }
    
    private String createErrorContext(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final String content) throws MessagingException {
        MimeMessage result = new MimeMessage(session);
        result.setFrom(new InternetAddress(config.getFrom()));
        result.setSubject(config.getSubject());
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        if (StringUtils.isNotBlank(config.getTo())) {
            result.addRecipient(Message.RecipientType.TO, new InternetAddress(config.getTo()));
        }
        if (StringUtils.isNotBlank(config.getCc())) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(config.getCc()));
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
