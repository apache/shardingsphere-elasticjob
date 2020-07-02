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

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic datasource config.
 */
@Configuration
public class DynamicDataSourceConfig {
    
    public static final String DRIVER_CLASS_NAME = "spring.datasource.default.driver-class-name";
    
    public static final String DATASOURCE_URL = "spring.datasource.default.url";
    
    public static final String DATASOURCE_USERNAME = "spring.datasource.default.username";
    
    public static final String DATASOURCE_PASSWORD = "spring.datasource.default.password";
    
    public static final String DEFAULT_DATASOURCE_NAME = "default";
    
    /**
     * Declare dynamicDataSource instead of default dataSource.
     * @param environment spring environment
     * @return A subClass of AbstractRoutingDataSource
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dynamicDataSource(final Environment environment) {
        DataSource defaultDataSource = createDefaultDataSource(environment);
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.addDataSource(DEFAULT_DATASOURCE_NAME, defaultDataSource);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }
    
    private DataSource createDefaultDataSource(final Environment environment) {
        String driverName = environment.getProperty(DRIVER_CLASS_NAME);
        String url = environment.getProperty(DATASOURCE_URL);
        String username = environment.getProperty(DATASOURCE_USERNAME);
        String password = environment.getProperty(DATASOURCE_PASSWORD);
        return DataSourceBuilder.create().driverClassName(driverName).type(BasicDataSource.class).url(url)
            .username(username).password(password).build();
    }
    
    public static class DynamicDataSource extends AbstractRoutingDataSource {
        
        private final Map<Object, Object> dataSourceMap = new HashMap<>(10);
        
        @Override
        protected Object determineCurrentLookupKey() {
            return DynamicDataSourceContextHolder.getDataSourceName();
        }
        
        /**
         * Add a data source.
         * 
         * @param dataSourceName data source name
         * @param dataSource data source
         */
        public void addDataSource(final String dataSourceName, final DataSource dataSource) {
            dataSourceMap.put(dataSourceName, dataSource);
            setTargetDataSources(dataSourceMap);
            afterPropertiesSet();
        }
    }
    
    public static class DynamicDataSourceContextHolder {
        
        private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
        
        /**
         * Get the specify dataSource.
         * 
         * @return data source name
         */
        public static String getDataSourceName() {
            return CONTEXT_HOLDER.get();
        }
        
        /**
         * Specify a dataSource.
         * 
         * @param dataSourceName data source name
         */
        public static void setDataSourceName(final String dataSourceName) {
            CONTEXT_HOLDER.set(dataSourceName);
        }
    }
}
