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

package org.apache.shardingsphere.elasticjob.tracing.rdb.yaml;

import org.apache.shardingsphere.elasticjob.tracing.rdb.config.RDBTracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.yaml.YamlTracingStorageConfiguration;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRDBTracingStorageConfigurationConverterTest {
    
    @Test
    void assertConvertDataSourceConfiguration() {
        RDBTracingStorageConfiguration dataSourceConfig = new RDBTracingStorageConfiguration("org.h2.Driver");
        dataSourceConfig.getProps().put("foo", "bar");
        YamlDataSourceConfigurationConverter converter = new YamlDataSourceConfigurationConverter();
        YamlTracingStorageConfiguration<DataSource> actual = converter.convertToYamlConfiguration(dataSourceConfig);
        assertTrue(actual instanceof YamlDataSourceConfiguration);
        YamlDataSourceConfiguration result = (YamlDataSourceConfiguration) actual;
        assertThat(result.getDataSourceClassName(), is("org.h2.Driver"));
        assertThat(result.getProps(), is(Collections.singletonMap("foo", "bar")));
    }
}
