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

package org.apache.shardingsphere.elasticjob.kernel.tracing.yaml;

import org.apache.shardingsphere.elasticjob.kernel.infra.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingStorageConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Converter to convert {@link TracingConfiguration} to {@link YamlTracingConfiguration}.
 *
 * @param <T> type of storage
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class YamlTracingConfigurationConverter<T> implements YamlConfigurationConverter<TracingConfiguration<T>, YamlTracingConfiguration<T>> {
    
    @Override
    public YamlTracingConfiguration<T> convertToYamlConfiguration(final TracingConfiguration<T> tracingConfig) {
        YamlTracingConfiguration<T> result = new YamlTracingConfiguration<>();
        result.setType(tracingConfig.getType());
        result.setTracingStorageConfiguration(convertTracingStorageConfiguration(tracingConfig.getTracingStorageConfiguration()));
        return result;
    }
    
    private YamlTracingStorageConfiguration<T> convertTracingStorageConfiguration(final TracingStorageConfiguration<T> tracingStorageConfig) {
        return (YamlTracingStorageConfiguration) TypedSPILoader.getService(YamlConfigurationConverter.class, tracingStorageConfig.getClass()).convertToYamlConfiguration(tracingStorageConfig);
    }
    
    @Override
    public Class getType() {
        return TracingConfiguration.class;
    }
}
