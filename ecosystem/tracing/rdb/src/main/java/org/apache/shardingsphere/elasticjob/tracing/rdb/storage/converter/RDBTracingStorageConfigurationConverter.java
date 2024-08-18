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

package org.apache.shardingsphere.elasticjob.tracing.rdb.storage.converter;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.spi.tracing.storage.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.tracing.exception.TracingStorageUnavailableException;
import org.apache.shardingsphere.elasticjob.spi.tracing.storage.TracingStorageConfigurationConverter;
import org.apache.shardingsphere.elasticjob.tracing.rdb.config.RDBTracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.datasource.DataSourceRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * RDB tracing storage converter.
 */
@Slf4j
public final class RDBTracingStorageConfigurationConverter implements TracingStorageConfigurationConverter<DataSource> {
    
    @Override
    public TracingStorageConfiguration<DataSource> toConfiguration(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            log.trace("Try to get connection from {}", connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new TracingStorageUnavailableException(ex);
        }
        RDBTracingStorageConfiguration result = RDBTracingStorageConfiguration.getDataSourceConfiguration(dataSource);
        DataSourceRegistry.getInstance().registerDataSource(result, dataSource);
        return result;
    }
    
    @Override
    public Class<DataSource> storageType() {
        return DataSource.class;
    }
}
