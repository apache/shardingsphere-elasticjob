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

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.rdb.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.yaml.YamlTracingStorageConfiguration;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML DataSourceConfiguration.
 */
@Setter
@Getter
public final class YamlDataSourceConfiguration implements YamlTracingStorageConfiguration<DataSource> {
    
    private static final long serialVersionUID = -8013707594458676772L;
    
    private String dataSourceClassName;
    
    private Map<String, Object> props = new LinkedHashMap<>();
    
    @Override
    public TracingStorageConfiguration<DataSource> toConfiguration() {
        DataSourceConfiguration result = new DataSourceConfiguration(dataSourceClassName);
        result.getProps().putAll(props);
        return result;
    }
}
