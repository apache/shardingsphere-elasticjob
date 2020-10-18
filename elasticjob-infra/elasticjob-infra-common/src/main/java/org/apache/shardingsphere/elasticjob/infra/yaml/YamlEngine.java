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

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.json.GsonFactory;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
    
    /**
     * Unmarshal YAML.
     *
     * @param prefix config prefix name
     * @param configFileInput YAML file input stream
     * @param classType class type
     * @param <T> type of class
     * @return object from YAML
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T unmarshal(final String prefix, final InputStream configFileInput, final Class<T> classType) {
        Map<String, Object> configDataMap = new Yaml().loadAs(configFileInput, Map.class);
        if (null != configDataMap && StringUtils.isNotBlank(prefix)) {
            List<String> prefixStrList = Splitter.on(".").trimResults().omitEmptyStrings().splitToList(prefix);
            for (String prefixStr : prefixStrList) {
                Object configData = configDataMap.get(prefixStr);
                if (configData instanceof Map) {
                    configDataMap = (Map) configData;
                } else {
                    configDataMap = null;
                    break;
                }
            }
        }
        if (null != configDataMap) {
            return GsonFactory.getGson().fromJson(GsonFactory.getGson().toJson(configDataMap), classType);
        }
        return null;
    }
}
