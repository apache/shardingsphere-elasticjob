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

package org.apache.shardingsphere.elasticjob.infra.yaml.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Factory for {@link YamlConfigurationConverter}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlConfigurationConverterFactory {
    
    private static final Map<Class<?>, YamlConfigurationConverter<?, ?>> CONVERTERS = new LinkedHashMap<>();
    
    static {
        ServiceLoader.load(YamlConfigurationConverter.class).forEach(each -> CONVERTERS.put(each.configurationType(), each));
    }
    
    /**
     * Find {@link YamlConfigurationConverter} for specific configuration type.
     *
     * @param configurationType type of configuration
     * @param <T> type of configuration
     * @param <Y> type of YAML configuration
     * @return converter for specific configuration type
     */
    @SuppressWarnings("unchecked")
    public static <T, Y extends YamlConfiguration<T>> Optional<YamlConfigurationConverter<T, Y>> findConverter(final Class<T> configurationType) {
        return Optional.ofNullable((YamlConfigurationConverter<T, Y>) CONVERTERS.get(configurationType));
    }
}
