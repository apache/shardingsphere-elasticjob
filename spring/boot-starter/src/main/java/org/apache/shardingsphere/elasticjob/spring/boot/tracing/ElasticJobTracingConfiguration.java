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

package org.apache.shardingsphere.elasticjob.spring.boot.tracing;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;

/**
 * ElasticJob tracing auto configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TracingProperties.class)
public class ElasticJobTracingConfiguration {
    
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "elasticjob.tracing.type", havingValue = "RDB")
    static class RDBTracingConfiguration {
        
        /**
         * Create a bean of tracing DataSource.
         *
         * @param tracingProperties tracing Properties
         * @return tracing DataSource
         */
        @Bean("tracingDataSource")
        public DataSource tracingDataSource(final TracingProperties tracingProperties) {
            DataSourceProperties dataSource = tracingProperties.getDataSource();
            if (dataSource == null) {
                return null;
            }
            HikariDataSource tracingDataSource = new HikariDataSource();
            tracingDataSource.setJdbcUrl(dataSource.getUrl());
            BeanUtils.copyProperties(dataSource, tracingDataSource);
            return tracingDataSource;
        }
        
        /**
         * Create a bean of tracing configuration.
         *
         * @param dataSource required by constructor
         * @param tracingDataSource tracing ataSource
         * @return a bean of tracing configuration
         */
        @Bean
        public TracingConfiguration<DataSource> tracingConfiguration(@Qualifier("dataSource") @Nullable final DataSource dataSource,
                                                                     @Qualifier("tracingDataSource") @Nullable final DataSource tracingDataSource) {
            if (dataSource == null && tracingDataSource == null) {
                throw new IllegalArgumentException("Neither dataSource nor tracingDataSource bean is required");
            }
            return new TracingConfiguration<>("RDB", null == tracingDataSource ? dataSource : tracingDataSource);
        }
    }
}
