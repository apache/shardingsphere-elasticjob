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

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

/**
 * Email configuration POJO.
 */
@Getter
@Setter
public final class EmailConfiguration {
    
    private String host;
    
    private String port;
    
    private String username;
    
    private String password;
    
    private String protocol;
    
    private String useSsl;
    
    private String subject;
    
    private String from;
    
    private String to;
    
    private String cc;
    
    private String bcc;
    
    private String debug;
    
    /**
     * Get email config.
     * @param props props
     * @return email config.
     */
    public static EmailConfiguration getByProps(final Properties props) {
        EmailConfiguration configuration = new EmailConfiguration();
        configuration.setHost(props.getProperty("email.host"));
        configuration.setPort(props.getProperty("email.port"));
        configuration.setUsername(props.getProperty("email.username"));
        configuration.setPassword(props.getProperty("email.password"));
        configuration.setProtocol(props.getOrDefault("email.protocol", "smtp").toString());
        configuration.setUseSsl(props.getProperty("email.useSsl"));
        configuration.setSubject(props.getOrDefault("email.subject", "ElasticJob error message").toString());
        configuration.setFrom(props.getProperty("email.form"));
        configuration.setTo(props.getProperty("email.to"));
        configuration.setCc(props.getProperty("email.cc"));
        configuration.setBcc(props.getProperty("email.bcc"));
        configuration.setDebug(props.getProperty("email.debug"));
        return configuration;
    }
}
