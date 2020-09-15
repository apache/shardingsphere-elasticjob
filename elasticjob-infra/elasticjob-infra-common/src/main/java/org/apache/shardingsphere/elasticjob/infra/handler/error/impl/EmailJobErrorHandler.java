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

package org.apache.shardingsphere.elasticjob.infra.handler.error.impl;

import com.google.common.base.Preconditions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.handler.error.JobErrorHandler;
import org.apache.shardingsphere.elasticjob.infra.pojo.EmailConfigurationPOJO;

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

    @Setter
    private EmailConfigurationPOJO emailConfigurationPOJO;

    private Session session;

    @Override
    public void handleException(final String jobName, final Throwable cause) {
        try {
            Preconditions.checkNotNull(emailConfigurationPOJO);
            String content = buildContent(jobName, cause);
            Message message = buildMessage(content);
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Elastic job: email job handler error", e);
        }
    }

    @Override
    public String getType() {
        return "EMAIL";
    }

    private Session buildSession() {
        if (null == session) {
            Properties props = new Properties();
            props.put("mail.smtp.host", emailConfigurationPOJO.getHost());
            props.put("mail.smtp.port", emailConfigurationPOJO.getPort());
            props.put("mail.smtp.auth", "true");
            props.put("mail.transport.protocol", emailConfigurationPOJO.getProtocol());
            session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailConfigurationPOJO.getUsername(), emailConfigurationPOJO.getPassword());
                }
            });
        }
        return session;
    }

    private Message buildMessage(final String content) throws MessagingException {
        MimeMessage message = new MimeMessage(buildSession());
        message.setFrom(new InternetAddress(emailConfigurationPOJO.getFrom()));
        message.setSubject(emailConfigurationPOJO.getSubject());
        message.setSentDate(new Date());
        Multipart multipart = new MimeMultipart();
        BodyPart mailBody = new MimeBodyPart();
        mailBody.setContent(content, "text/html; charset=utf-8");
        multipart.addBodyPart(mailBody);
        message.setContent(multipart);
        if (StringUtils.isNotBlank(emailConfigurationPOJO.getTo())) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfigurationPOJO.getTo()));
        }
        if (StringUtils.isNotBlank(emailConfigurationPOJO.getCc())) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailConfigurationPOJO.getCc()));
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
}
