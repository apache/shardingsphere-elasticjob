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

package org.apache.shardingsphere.elasticjob.error.handler.config;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Job error handler.
 */
@NoArgsConstructor
public class ConfigurationBuilder {

    private static final String DEFAULT_YAML_CONFIG_PATH = "ejob-config.yaml";

    /**
     * Unmarshal YAML.
     *
     * @param prefix config prefix nametent
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     */
    public static <T> T buildConfigByYaml(final String prefix, final Class<T> classType) {
        URL configFileUrl = ClassLoader.getSystemResource(DEFAULT_YAML_CONFIG_PATH);
        URI configFileUri;
        try {
            configFileUri = new URI(StringUtils.replace(configFileUrl.toString(), " ", "%20"));
            File configFile = new File(configFileUri);
            if (configFile.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(configFile);
                    return YamlEngine.unmarshal(prefix, inputStream, classType);
                } catch (FileNotFoundException ignore) {
                }
            }
        } catch (URISyntaxException ignore) {
        }
        return null;
    }
}
