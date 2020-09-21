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

package org.apache.shardingsphere.elasticjob.error.handler.env;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.error.handler.config.DingtalkConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Bootstrap env.
 */
@Slf4j
public final class DingtalkEnvironment {
    
    private static final DingtalkEnvironment INSTANCE = new DingtalkEnvironment();
    
    private static final String PROPERTIES_PATH = "conf/elasticjob-dingtalk.properties";
    
    private static final String CONFIG_PREFIX = "elasticjob.dingtalk";
    
    private final Properties properties;
    
    private DingtalkEnvironment() {
        properties = getProperties();
    }
    
    /**
     * Get instance of Dingtalk env.
     *
     * @return instance of Dingtalk env.
     */
    public static DingtalkEnvironment getInstance() {
        return INSTANCE;
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        try (InputStream fileInputStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            if (fileInputStream != null) {
                result.load(fileInputStream);
            }
        } catch (final IOException ex) {
            log.warn("Can not load properties file from path: '{}'.", PROPERTIES_PATH);
        }
        setPropertiesByEnv(result);
        return result;
    }
    
    private void setPropertiesByEnv(final Properties prop) {
        for (EnvironmentArgument each : EnvironmentArgument.values()) {
            String key = each.getKey();
            String value = System.getProperties().getProperty(String.join(".", CONFIG_PREFIX, key));
            if (!Strings.isNullOrEmpty(value)) {
                log.info("Load property {} with value {} from ENV.", key, value);
                prop.setProperty(each.getKey(), value);
            }
        }
    }
    
    /**
     * Get dingtalk configuration.
     *
     * @return dingtalk configuration
     */
    public DingtalkConfiguration getDingtalkConfiguration() {
        String webhook = getValue(EnvironmentArgument.WEBHOOK);
        String keyword = getValue(EnvironmentArgument.KEYWORD);
        String secret = getValue(EnvironmentArgument.SECRET);
        String connectTimeout = getValue(EnvironmentArgument.CONNECT_TIMEOUT);
        String readTimeout = getValue(EnvironmentArgument.READ_TIMEOUT);
        return new DingtalkConfiguration(webhook, keyword, secret, Integer.parseInt(connectTimeout), Integer.parseInt(readTimeout));
    }
    
    private String getValue(final EnvironmentArgument environmentArgument) {
        String result = properties.getProperty(environmentArgument.getKey(), environmentArgument.getDefaultValue());
        if (environmentArgument.isRequired()) {
            Preconditions.checkState(!Strings.isNullOrEmpty(result), String.format("Property `%s` is required.", environmentArgument.getKey()));
        }
        return result;
    }
    
    /**
     * Env args.
     */
    @RequiredArgsConstructor
    @Getter
    public enum EnvironmentArgument {
        
        WEBHOOK("webhook", "", true),
        
        KEYWORD("keyword", "", false),
        
        SECRET("secret", "", false),
        
        CONNECT_TIMEOUT("connectTimeout", "3000", false),
        
        READ_TIMEOUT("readTimeout", "5000", false);
        
        private final String key;
        
        private final String defaultValue;
        
        private final boolean required;
    }
}
