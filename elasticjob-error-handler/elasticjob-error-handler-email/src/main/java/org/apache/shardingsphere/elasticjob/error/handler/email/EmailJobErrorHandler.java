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
    
    public static final String CONFIG_PREFIX = "email";
    
    private EmailConfiguration emailConfiguration;
    
    private Session session;
    
    public EmailJobErrorHandler() {
        loadConfiguration();
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        if (null == emailConfiguration) {
            String errorMessage = String.format("An exception occurred in '%s' processing but failed to send email because no configuration found for email job error handler. "
                    + "Please configure email job error handler.", jobName);
            log.error(errorMessage, cause);
            return;
        }
        try {
            String content = buildContent(jobName, cause);
            Message message = buildMessage(content);
            sendMessage(message);
        } catch (final MessagingException ex) {
            log.error(String.format("Job '%s' exception occur in job processing", jobName), cause);
            log.error("An exception occurred but failed to send email because", ex);
        }
    }
    
    private void loadConfiguration() {
        emailConfiguration = Optional.ofNullable(ConfigurationLoader.buildConfigBySystemProperties())
                .orElseGet(() -> ConfigurationLoader.buildConfigByYaml(CONFIG_PREFIX));
        if (null == emailConfiguration) {
            log.warn("No configuration found for email job error handler. Please configure email job error handler if you are going to use it.");
        }
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
    
    private synchronized Session buildSession() {
        if (null == session) {
            Properties props = new Properties();
            props.put("mail.smtp.host", emailConfiguration.getHost());
            props.put("mail.smtp.port", emailConfiguration.getPort());
            props.put("mail.smtp.auth", "true");
            props.put("mail.transport.protocol", emailConfiguration.getProtocol());
            props.setProperty("mail.debug", Boolean.toString(emailConfiguration.isDebug()));
            if (emailConfiguration.isUseSsl()) {
                props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.smtp.socketFactory.fallback", "false");
            }
            session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfiguration.getUsername(), emailConfiguration.getPassword());
                }
            });
        }
        return session;
    }
    
    private Message buildMessage(final String content) throws MessagingException {
        MimeMessage message = new MimeMessage(Optional.ofNullable(session).orElseGet(this::buildSession));
        message.setFrom(new InternetAddress(emailConfiguration.getFrom()));
        message.setSubject(emailConfiguration.getSubject());
        message.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        message.setContent(multipart);
        if (StringUtils.isNotBlank(emailConfiguration.getTo())) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfiguration.getTo()));
        }
        if (StringUtils.isNotBlank(emailConfiguration.getCc())) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailConfiguration.getCc()));
        }
        message.saveChanges();
        return message;
    }
    
    private String buildContent(final String jobName, final Throwable cause) {
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw, true));
        String causeString = sw.toString();
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, causeString);
    }
    
    private void sendMessage(final Message message) throws MessagingException {
        Transport.send(message);
    }
}
