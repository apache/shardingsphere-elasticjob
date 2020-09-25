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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.io.InputStream;

/**
 * Job error configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationLoader {
    
    private static final String ERROR_HANDLER_CONFIG = "conf/error-handler-email.yaml";
    
    /**
     * Unmarshal YAML.
     *
     * @param prefix    config prefix
     * @return object from YAML
     */
    public static EmailConfiguration buildConfigByYaml(final String prefix) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_HANDLER_CONFIG);
        return YamlEngine.unmarshal(prefix, inputStream, EmailConfiguration.class);
    }
    
    /**
     * read system properties.
     *
     * @return object from system properties
     */
    public static EmailConfiguration buildConfigBySystemProperties() {
        String isBySystemProperties = System.getProperty("error-handler-email.use-system-properties");
        if (!Boolean.valueOf(isBySystemProperties)) {
            return null;
        }
        EmailConfiguration emailConfiguration = new EmailConfiguration();
        emailConfiguration.setHost(System.getProperty("error-handler-email.host"));
        emailConfiguration.setUsername(System.getProperty("error-handler-email.username"));
        emailConfiguration.setPassword(System.getProperty("error-handler-email.password"));
        emailConfiguration.setProtocol(System.getProperty("error-handler-email.protocol"));
        emailConfiguration.setFrom(System.getProperty("error-handler-email.from"));
        emailConfiguration.setTo(System.getProperty("error-handler-email.to"));
        emailConfiguration.setCc(System.getProperty("error-handler-email.cc"));
        emailConfiguration.setBcc(System.getProperty("error-handler-email.bcc"));
        String port = System.getProperty("error-handler-email.port");
        if (null != port) {
            emailConfiguration.setPort(Integer.valueOf(port));
        }
        return emailConfiguration;
    }
}
