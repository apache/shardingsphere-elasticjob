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
 * Job error handler for send error message via email.
 */
@Slf4j
public final class EmailJobErrorHandler implements JobErrorHandler {
    
    private Session session;
    
    private String subject;
    
    private String from;
    
    private String to;
    
    private String cc;
    
    private String bcc;
    
    @Override
    public void init(final Properties props) {
        String host = props.getProperty(EmailPropertiesConstants.HOST);
        int port = Integer.parseInt(props.getProperty(EmailPropertiesConstants.PORT));
        boolean isUseSSL = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_USE_SSL, EmailPropertiesConstants.DEFAULT_IS_USE_SSL));
        boolean isDebug = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_DEBUG, EmailPropertiesConstants.DEFAULT_IS_DEBUG));
        String username = props.getProperty(EmailPropertiesConstants.USERNAME);
        String password = props.getProperty(EmailPropertiesConstants.PASSWORD);
        session = Session.getDefaultInstance(createSessionProperties(host, port, isUseSSL, isDebug), getSessionAuthenticator(username, password));
        subject = props.getProperty(EmailPropertiesConstants.SUBJECT, EmailPropertiesConstants.DEFAULT_SUBJECT);
        from = props.getProperty(EmailPropertiesConstants.FROM);
        to = props.getProperty(EmailPropertiesConstants.TO);
        cc = props.getProperty(EmailPropertiesConstants.CC);
        bcc = props.getProperty(EmailPropertiesConstants.BCC);
    }
    
    private Properties createSessionProperties(final String host, final int port, final boolean isUseSSL, final boolean isDebug) {
        Properties result = new Properties();
        result.put("mail.smtp.host", host);
        result.put("mail.smtp.port", port);
        result.put("mail.smtp.auth", Boolean.TRUE.toString());
        result.put("mail.transport.protocol", "smtp");
        result.setProperty("mail.debug", Boolean.toString(isDebug));
        if (isUseSSL) {
            result.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            result.setProperty("mail.smtp.socketFactory.fallback", Boolean.FALSE.toString());
        }
        return result;
    }
    
    private Authenticator getSessionAuthenticator(final String username, final String password) {
        return new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        String errorMessage = getErrorMessage(jobName, cause);
        try {
            sendMessage(createMessage(errorMessage));
            log.info("An exception has occurred in Job '{}', an email has been sent successfully.", jobName, cause);
        } catch (final MessagingException ex) {
            cause.addSuppressed(ex);
            log.error("An exception has occurred in Job '{}' but failed to send email because of", jobName, cause);
        }
    }
    
    private String getErrorMessage(final String jobName, final Throwable cause) {
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer, true));
        return String.format("Job '%s' exception occur in job processing, caused by %s", jobName, writer.toString());
    }
    
    private Message createMessage(final String content) throws MessagingException {
        MimeMessage result = new MimeMessage(session);
        result.setFrom(new InternetAddress(from));
        result.setSubject(subject);
        result.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        result.setContent(multipart);
        if (StringUtils.isNotBlank(to)) {
            String[] tos = to.split(",");
            for (String each : tos) {
                result.addRecipient(Message.RecipientType.TO, new InternetAddress(each));
            }
        }
        if (!Strings.isNullOrEmpty(cc)) {
            result.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        }
        if (!Strings.isNullOrEmpty(bcc)) {
            result.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
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
