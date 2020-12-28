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

package org.apache.shardingsphere.elasticjob.tracing.rdb.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingStorageConfiguration;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mapping {@link TracingStorageConfiguration} to {@link DataSource}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceRegistry {
    
    private static volatile DataSourceRegistry instance;
    
    private final ConcurrentMap<DataSourceConfiguration, DataSource> dataSources = new ConcurrentHashMap<>();
    
    /**
     * Get instance of {@link DataSourceRegistry}.
     *
     * @return {@link DataSourceRegistry} singleton
     */
    public static DataSourceRegistry getInstance() {
        if (null == instance) {
            synchronized (DataSourceRegistry.class) {
                if (null == instance) {
                    instance = new DataSourceRegistry();
                }
            }
        }
        return instance;
    }
    
    void registerDataSource(final DataSourceConfiguration configuration, final DataSource dataSource) {
        dataSources.putIfAbsent(configuration, dataSource);
    }
    
    /**
     * Get {@link DataSource} by {@link TracingStorageConfiguration}.
     *
     * @param configuration {@link TracingStorageConfiguration}
     * @return instance of {@link DataSource}
     */
    public DataSource getDataSource(final DataSourceConfiguration configuration) {
        return dataSources.computeIfAbsent(configuration, DataSourceConfiguration::createDataSource);
    }
}
