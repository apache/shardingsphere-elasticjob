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

package org.apache.shardingsphere.elasticjob.error.handler.email.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * Job error handler configuration for send error message via email.
 */
@Getter
@Setter
public final class EmailConfiguration {
    
    private String host;
    
    private Integer port;
    
    private String username;
    
    private String password;
    
    private boolean useSsl;
    
    private String subject;
    
    private String from;
    
    private String to;
    
    private String cc;
    
    private String bcc;
    
    private boolean debug;
    
    /**
     * Get email configuration.
     *
     * @param props properties
     * @return email configuration
     */
    public static EmailConfiguration getByProps(final Properties props) {
        EmailConfiguration result = new EmailConfiguration();
        result.setHost(props.getProperty(EmailConstants.EMAIL_HOST));
        result.setPort(Integer.parseInt(props.getProperty(EmailConstants.EMAIL_PORT)));
        result.setUsername(props.getProperty(EmailConstants.EMAIL_USERNAME));
        result.setPassword(props.getProperty(EmailConstants.EMAIL_PASSWORD));
        result.setUseSsl(Boolean.parseBoolean(props.getOrDefault(EmailConstants.EMAIL_USE_SSL, Boolean.FALSE.toString()).toString()));
        result.setSubject(props.getOrDefault(EmailConstants.EMAIL_SUBJECT, EmailConstants.DEFAULT_EMAIL_SUBJECT).toString());
        result.setFrom(props.getProperty(EmailConstants.EMAIL_FROM));
        result.setTo(props.getProperty(EmailConstants.EMAIL_TO));
        result.setCc(props.getProperty(EmailConstants.EMAIL_CC));
        result.setBcc(props.getProperty(EmailConstants.EMAIL_BCC));
        result.setDebug(Boolean.parseBoolean(props.getOrDefault(EmailConstants.EMAIL_DEBUG, Boolean.FALSE.toString()).toString()));
        return result;
    }
}
