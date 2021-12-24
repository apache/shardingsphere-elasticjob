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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingStorageConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.storage.TracingStorageConverter;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingStorageUnavailableException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link TracingStorageConverter} for {@link DataSource}.
 */
@Slf4j
public final class DataSourceTracingStorageConverter implements TracingStorageConverter<DataSource> {
    
    @Override
    public TracingStorageConfiguration<DataSource> convertObjectToConfiguration(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            log.trace("Try to get connection from {}", connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            throw new TracingStorageUnavailableException(ex);
        }
        DataSourceConfiguration result = DataSourceConfiguration.getDataSourceConfiguration(dataSource);
        DataSourceRegistry.getInstance().registerDataSource(result, dataSource);
        return result;
    }
    
    @Override
    public Class<DataSource> storageType() {
        return DataSource.class;
    }
}
