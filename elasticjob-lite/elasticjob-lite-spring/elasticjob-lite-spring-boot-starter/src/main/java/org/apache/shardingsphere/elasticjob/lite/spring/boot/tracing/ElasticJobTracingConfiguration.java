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

package org.apache.shardingsphere.elasticjob.lite.spring.boot.tracing;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;

/**
 * ElasticJob tracing auto configuration.
 */
@EnableConfigurationProperties(TracingProperties.class)
public class ElasticJobTracingConfiguration {

    /**
     * Create a bean of tracing DataSource.
     *
     * @param tracingProperties tracing Properties
     * @return tracing DataSource
     */
    @Bean("tracingDataSource")
    public DataSource tracingDataSource(final TracingProperties tracingProperties) {
        if (tracingProperties.getDataSource() == null) {
            return null;
        }
        HikariDataSource tracingDataSource = new HikariDataSource();
        tracingDataSource.setJdbcUrl(tracingProperties.getDataSource().getUrl());
        BeanUtils.copyProperties(tracingProperties.getDataSource(), tracingDataSource);
        return tracingDataSource;
    }

    /**
     * Create a bean of tracing configuration.
     *
     * @param dataSource required by constructor
     * @return a bean of tracing configuration
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "elasticjob.tracing.type", havingValue = "RDB")
    public TracingConfiguration<DataSource> tracingConfiguration(final DataSource dataSource, @Nullable final DataSource tracingDataSource) {
        DataSource ds = tracingDataSource;
        if (ds == null) {
            ds = dataSource;
        }
        return new TracingConfiguration<>("RDB", ds);
    }
}
