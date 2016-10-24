/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.event.rdb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class JobEventRdbDataSourceFactory {
    
    private static final ConcurrentHashMap<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();
    
    static DataSource getDataSource(final String driverClassName, final String url, final String username, final String password) {
        DataSource dataSource = createDataSource(driverClassName, url, username, password);
        DATA_SOURCES.putIfAbsent(url, dataSource);
        return null == DATA_SOURCES.get(url) ? dataSource : DATA_SOURCES.get(url);
    }
    
    private static DataSource createDataSource(final String driverClassName, final String url, final String username, final String password) {
        // TODO 细化pool配置
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(driverClassName);
        result.setUrl(url);
        result.setUsername(username);
        result.setPassword(password);
        return result;
    }
}
