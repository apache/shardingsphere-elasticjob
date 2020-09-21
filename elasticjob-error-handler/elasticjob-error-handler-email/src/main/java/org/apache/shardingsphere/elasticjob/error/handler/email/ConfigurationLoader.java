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

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;

import java.io.InputStream;

/**
 * Job error configuration loader.
 */
@NoArgsConstructor
public class ConfigurationLoader {
    
    private static final String ERROR_HANDLER_CONFIG = "error-handler-email.yaml";
    
    /**
     * Unmarshal YAML.
     *
     * @param prefix    config prefix
     * @param classType class type
     * @param <T>       type of class
     * @return object from YAML
     */
    public static <T> T buildConfigByYaml(final String prefix, final Class<T> classType) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_HANDLER_CONFIG);
        return YamlEngine.unmarshal(prefix, inputStream, classType);
    }
}
