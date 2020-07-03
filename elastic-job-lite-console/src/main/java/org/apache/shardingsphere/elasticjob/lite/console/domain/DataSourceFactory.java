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

package org.apache.shardingsphere.elasticjob.lite.console.domain;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

/**
 * Dynamic data source factory.
 */
public final class DataSourceFactory {
    
    /**
     * Create a DataSource.
     * @param config event trace data source config
     * @return data source
     */
    public static DataSource createDataSource(final EventTraceDataSourceConfiguration config) {
        // Determine whether the data source is valid.
        new EventTraceDataSource(config).init();
        return DataSourceBuilder.create().type(BasicDataSource.class).driverClassName(config.getDriver()).url(config.getUrl())
            .username(config.getUsername()).password(config.getPassword()).build();
    }
}
