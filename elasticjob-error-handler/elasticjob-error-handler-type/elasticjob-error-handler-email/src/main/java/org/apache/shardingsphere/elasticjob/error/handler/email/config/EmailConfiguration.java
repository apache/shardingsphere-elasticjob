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

import java.util.Properties;

/**
 * Job error handler configuration for send error message via email.
 */
@Getter
public final class EmailConfiguration {
    
    private final String host;
    
    private final int port;
    
    private final String username;
    
    private final String password;
    
    private final boolean useSsl;
    
    private final String subject;
    
    private final String from;
    
    private final String to;
    
    private final String cc;
    
    private final String bcc;
    
    private final boolean debug;
    
    public EmailConfiguration(final Properties props) {
        host = props.getProperty(EmailPropertiesConstants.HOST);
        port = Integer.parseInt(props.getProperty(EmailPropertiesConstants.PORT));
        username = props.getProperty(EmailPropertiesConstants.USERNAME);
        password = props.getProperty(EmailPropertiesConstants.PASSWORD);
        useSsl = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_USE_SSL, Boolean.FALSE.toString()));
        subject = props.getProperty(EmailPropertiesConstants.SUBJECT, EmailPropertiesConstants.DEFAULT_SUBJECT);
        from = props.getProperty(EmailPropertiesConstants.FROM);
        to = props.getProperty(EmailPropertiesConstants.TO);
        cc = props.getProperty(EmailPropertiesConstants.CC);
        bcc = props.getProperty(EmailPropertiesConstants.BCC);
        debug = Boolean.parseBoolean(props.getProperty(EmailPropertiesConstants.IS_DEBUG, Boolean.FALSE.toString()));
    }
}
