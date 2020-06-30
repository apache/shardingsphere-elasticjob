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

package org.apache.shardingsphere.elasticjob.lite.console.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Dynamic datasource config.
 */
@Configuration
public class DynamicDataSourceConfig {

    public static DataSource createDefaultDs(Environment environment) {
        String driverName = environment.getProperty("spring.datasource.default.driver-class-name");
        String url = environment.getProperty("spring.datasource.default.url");
        String username = environment.getProperty("spring.datasource.default.username");
        String password = environment.getProperty("spring.datasource.default.password");
        return DataSourceBuilder.create().driverClassName(driverName).type(BasicDataSource.class).url(url)
            .username(username).password(password).build();
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dynamicDataSource(Environment environment) {
        DataSource defaultDataSource = createDefaultDs(environment);
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.dsMap.put("default", defaultDataSource);
        dynamicDataSource.setTargetDataSources(dynamicDataSource.dsMap);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }

    public static class DynamicDataSource extends AbstractRoutingDataSource {

        private final Map<Object, Object> dsMap = new HashMap<>();

        @Override
        protected Object determineCurrentLookupKey() {
            return DynamicDataSourceContextHolder.getDataSourceName();
        }
    }

    public static class DynamicDataSourceContextHolder {

        private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

        public static void setDataSourceName(String dsName) {
            contextHolder.set(dsName);
        }

        public static String getDataSourceName() {
            return contextHolder.get();
        }
    }
}
