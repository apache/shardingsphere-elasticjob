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

package org.apache.shardingsphere.elasticjob.tracing.boot;

import org.apache.shardingsphere.elasticjob.lite.boot.ElasticJobLiteAutoConfiguration;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(ElasticJobLiteAutoConfiguration.class)
public class ElasticJobTracingAutoConfiguration {

    /**
     * Create a bean of TracingConfiguration.
     *
     * @param dataSource required by constructor
     * @return a bean of TracingConfiguration
     */
    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "elasticjob.tracing.type", havingValue = "RDB")
    public TracingConfiguration<?> tracingConfiguration(final DataSource dataSource) {
        return new TracingConfiguration<>("RDB", dataSource);
    }
}
