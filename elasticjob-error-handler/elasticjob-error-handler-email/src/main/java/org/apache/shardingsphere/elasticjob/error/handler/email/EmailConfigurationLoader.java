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
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.io.InputStream;

/**
 * Job error configuration loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailConfigurationLoader {
    
    private static final String CONFIG_FILE = "conf/error-handler-email.yaml";
    
    /**
     * Unmarshal YAML.
     *
     * @param prefix    config prefix
     * @return object from YAML
     */
    public static EmailConfiguration buildConfigByYaml(final String prefix) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
        return YamlEngine.unmarshal(prefix, inputStream, EmailConfiguration.class);
    }
    
    /**
     * Read system properties.
     *
     * @return object from system properties
     */
    public static EmailConfiguration buildConfigBySystemProperties() {
        String isBySystemProperties = System.getProperty("error-handler-email.use-system-properties");
        if (!Boolean.parseBoolean(isBySystemProperties)) {
            return null;
        }
        EmailConfiguration result = new EmailConfiguration();
        result.setHost(System.getProperty("error-handler-email.host"));
        result.setUsername(System.getProperty("error-handler-email.username"));
        result.setPassword(System.getProperty("error-handler-email.password"));
        result.setFrom(System.getProperty("error-handler-email.from"));
        result.setTo(System.getProperty("error-handler-email.to"));
        result.setCc(System.getProperty("error-handler-email.cc"));
        result.setBcc(System.getProperty("error-handler-email.bcc"));
        String protocol = System.getProperty("error-handler-email.protocol");
        if (StringUtils.isNotBlank(protocol)) {
            result.setProtocol(System.getProperty("error-handler-email.protocol"));
        }
        String useSSL = System.getProperty("error-handler-email.use-ssl");
        if (StringUtils.isNotBlank(useSSL)) {
            result.setUseSsl(Boolean.parseBoolean(useSSL));
        }
        String subject = System.getProperty("error-handler-email.subject");
        if (StringUtils.isNotBlank(subject)) {
            result.setSubject(subject);
        }
        String port = System.getProperty("error-handler-email.port");
        if (StringUtils.isNotBlank(port)) {
            result.setPort(Integer.valueOf(port));
        }
        String debug = System.getProperty("error-handler-email.debug");
        if (StringUtils.isNotBlank(debug)) {
            result.setDebug(Boolean.parseBoolean(debug));
        }
        return result;
    }
}
