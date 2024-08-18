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

import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.fixture.config.TracingStorageFixture;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlTracingConfigurationConverterTest {
    
    @Test
    void assertConvertTracingConfiguration() {
        TracingStorageFixture expectedStorage = () -> {
        };
        TracingConfiguration<TracingStorageFixture> tracingConfig = new TracingConfiguration<>("TEST", expectedStorage);
        YamlTracingConfigurationConverter<TracingStorageFixture> converter = new YamlTracingConfigurationConverter<>();
        YamlTracingConfiguration<TracingStorageFixture> actual = converter.convertToYamlConfiguration(tracingConfig);
        assertThat(actual.getType(), is("TEST"));
        assertNotNull(actual.getTracingStorageConfiguration());
        assertTrue(actual.getTracingStorageConfiguration() instanceof YamlJobEventCallerConfiguration);
        YamlJobEventCallerConfiguration result = (YamlJobEventCallerConfiguration) actual.getTracingStorageConfiguration();
        assertThat(result.getTracingStorageFixture(), is(expectedStorage));
    }
}
