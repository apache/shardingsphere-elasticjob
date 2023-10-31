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
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.fixture.config.TracingStorageFixture;
import org.apache.shardingsphere.elasticjob.kernel.tracing.fixture.config.TracingStorageConfigurationFixture;

/**
 * YAML job event caller configuration converter.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class YamlJobEventCallerConfigurationConverter
        implements
            YamlConfigurationConverter<TracingStorageConfiguration<TracingStorageFixture>, YamlTracingStorageConfiguration<TracingStorageFixture>> {
    
    @Override
    public YamlTracingStorageConfiguration<TracingStorageFixture> convertToYamlConfiguration(final TracingStorageConfiguration<TracingStorageFixture> data) {
        YamlJobEventCallerConfiguration result = new YamlJobEventCallerConfiguration();
        result.setTracingStorageFixture(data.getStorage());
        return result;
    }
    
    @Override
    public Class getType() {
        return TracingStorageConfigurationFixture.class;
    }
}
