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
 * Job error handler for send error message via email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    private Session session;
    
    @Override
    public void handleException(final String jobName, final Properties props, final Throwable cause) {
        String errorMessage = getErrorMessage(jobName, cause);
        EmailConfiguration config = createConfiguration(props);
        try {
            sendMessage(createMessage(errorMessage, config), config);
            log.error("An exception has occurred in Job '{}'. An email has been sent successfully.", jobName, cause);
        } catch (final MessagingException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}' but failed to send email because of", jobName, cause);
        }
    }
    
    private EmailConfiguration createConfiguration(final Properties props) {
        String host = props.getProperty(EmailPropertiesConstants.HOST);
        int port = Integer.parseInt(props.getProperty(EmailPropertiesConstants.PORT));
        String username = props.getProperty(EmailPropertiesConstants.USERNAME);
        String password = props.getProperty(EmailPropertiesConstants.PASSWORD);
        boolean isUseSSL = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_USE_SSL, EmailPropertiesConstants.DEFAULT_IS_USE_SSL));
        String subject = props.getProperty(EmailPropertiesConstants.SUBJECT, EmailPropertiesConstants.DEFAULT_SUBJECT);
        String from = props.getProperty(EmailPropertiesConstants.FROM);
        String to = props.getProperty(EmailPropertiesConstants.TO);
        String cc = props.getProperty(EmailPropertiesConstants.CC);
        String bcc = props.getProperty(EmailPropertiesConstants.BCC);
        boolean isDebug = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_DEBUG, EmailPropertiesConstants.DEFAULT_IS_DEBUG));
        return new EmailConfiguration(host, port, username, password, isUseSSL, subject, from, to, cc, bcc, isDebug);
    }
    
    private String getErrorMessage(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final String content, final EmailConfiguration config) throws MessagingException {
        MimeMessage result = new MimeMessage(Optional.ofNullable(session).orElseGet(() -> createSession(config)));
        result.setFrom(new InternetAddress(config.getFrom()));
        result.setSubject(config.getSubject());
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        String to = config.getTo();
        if (StringUtils.isNotBlank(to)) {
            String[] tos = to.split(",");
            for (String each : tos) {
                result.addRecipient(Message.RecipientType.TO, new InternetAddress(each));
            }
        }
        if (StringUtils.isNotBlank(config.getCc())) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(config.getCc()));
        }
        if (StringUtils.isNotBlank(config.getBcc())) {
            result.addRecipient(Message.RecipientType.BCC, new InternetAddress(config.getBcc()));
        }
        result.saveChanges();
        return result;
    }
    
    private void sendMessage(final Message message, final EmailConfiguration config) throws MessagingException {
        try (Transport transport = Optional.ofNullable(session).orElseGet(() -> createSession(config)).getTransport()) {
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
    
    private synchronized Session createSession(final EmailConfiguration config) {
        if (null == session) {
            session = Session.getDefaultInstance(createSessionProperties(config), getSessionAuthenticator(config));
        }
        return session;
    }
    
    private Properties createSessionProperties(final EmailConfiguration config) {
        Properties result = new Properties();
        result.put("mail.smtp.host", config.getHost());
        result.put("mail.smtp.port", config.getPort());
        result.put("mail.smtp.auth", "true");
        result.put("mail.transport.protocol", "smtp");
        result.setProperty("mail.debug", Boolean.toString(config.isDebug()));
        if (config.isUseSsl()) {
            result.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            result.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        return result;
    }
    
    private Authenticator getSessionAuthenticator(final EmailConfiguration config) {
        return new Authenticator() {
            
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        };
    }
    
    @Override
    public String getType() {
        return "EMAIL";
    }
}
