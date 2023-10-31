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

import org.apache.shardingsphere.elasticjob.kernel.infra.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.elasticjob.kernel.tracing.api.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.yaml.YamlTracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.rdb.datasource.RDBTracingStorageConfiguration;

import javax.sql.DataSource;

/**
 * {@link YamlConfigurationConverter} for {@link YamlDataSourceConfiguration}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class YamlDataSourceConfigurationConverter implements YamlConfigurationConverter<TracingStorageConfiguration<DataSource>, YamlTracingStorageConfiguration<DataSource>> {
    
    @Override
    public YamlTracingStorageConfiguration<DataSource> convertToYamlConfiguration(final TracingStorageConfiguration<DataSource> data) {
        RDBTracingStorageConfiguration dataSourceConfig = (RDBTracingStorageConfiguration) data;
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName(dataSourceConfig.getDataSourceClassName());
        result.setProps(dataSourceConfig.getProps());
        return result;
    }
    
    @Override
    public Class getType() {
        return RDBTracingStorageConfiguration.class;
    }
}
