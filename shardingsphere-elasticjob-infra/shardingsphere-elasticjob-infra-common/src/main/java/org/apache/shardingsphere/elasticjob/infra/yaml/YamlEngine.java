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

package org.apache.shardingsphere.elasticjob.infra.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.infra.yaml.representer.ElasticJobYamlRepresenter;
import org.yaml.snakeyaml.Yaml;

/**
 * YAML engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlEngine {
    
    /**
     * Marshal YAML.
     *
     * @param value object to be marshaled
     * @return YAML content
     */
    public static String marshal(final Object value) {
        return new Yaml(new ElasticJobYamlRepresenter()).dumpAsMap(value);
    }
    
    /**
     * Unmarshal YAML.
     *
     * @param yamlContent YAML content
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     */
    public static <T> T unmarshal(final String yamlContent, final Class<T> classType) {
        return new Yaml().loadAs(yamlContent, classType);
    }
}
