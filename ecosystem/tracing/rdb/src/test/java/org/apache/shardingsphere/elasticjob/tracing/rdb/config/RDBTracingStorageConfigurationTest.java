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

package org.apache.shardingsphere.elasticjob.tracing.rdb.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class RDBTracingStorageConfigurationTest {
    
    @Test
    void assertGetDataSourceConfiguration() throws SQLException {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setLoginTimeout(1);
        RDBTracingStorageConfiguration actual = RDBTracingStorageConfiguration.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
    }
    
    @Test
    void assertCreateDataSource() {
        Map<String, Object> props = new HashMap<>(16, 1);
        props.put("driverClassName", "org.h2.Driver");
        props.put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.put("username", "root");
        props.put("password", "root");
        props.put("loginTimeout", "5000");
        props.put("test", "test");
        RDBTracingStorageConfiguration dataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        dataSourceConfig.getProps().putAll(props);
        HikariDataSource actual = (HikariDataSource) dataSourceConfig.createDataSource();
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    void assertEquals() {
        RDBTracingStorageConfiguration originalDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        RDBTracingStorageConfiguration targetDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        assertThat(originalDataSourceConfig, is(originalDataSourceConfig));
        assertThat(originalDataSourceConfig, is(targetDataSourceConfig));
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        assertThat(originalDataSourceConfig, is(targetDataSourceConfig));
    }
    
    @Test
    void assertNotEquals() {
        RDBTracingStorageConfiguration originalDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        RDBTracingStorageConfiguration targetDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root0");
        assertThat(originalDataSourceConfig, not(targetDataSourceConfig));
    }
    
    @Test
    void assertEqualsWithNull() {
        assertFalse(new RDBTracingStorageConfiguration(HikariDataSource.class.getName()).equals(null));
    }
    
    @Test
    void assertSameHashCode() {
        RDBTracingStorageConfiguration originalDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        RDBTracingStorageConfiguration targetDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig.getProps().put("password", "root");
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.hashCode(), is(targetDataSourceConfig.hashCode()));
    }
    
    @Test
    void assertDifferentHashCode() {
        RDBTracingStorageConfiguration originalDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        RDBTracingStorageConfiguration targetDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        originalDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("username", "root");
        targetDataSourceConfig.getProps().put("password", "root");
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
        originalDataSourceConfig = new RDBTracingStorageConfiguration(HikariDataSource.class.getName());
        targetDataSourceConfig = new RDBTracingStorageConfiguration(DataSource.class.getName());
        assertThat(originalDataSourceConfig.hashCode(), not(targetDataSourceConfig.hashCode()));
    }
    
    @Test
    void assertGetDataSourceConfigurationWithConnectionInitSqls() {
        HikariDataSource actualDataSource = new HikariDataSource();
        actualDataSource.setDriverClassName("org.h2.Driver");
        actualDataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        actualDataSource.setUsername("root");
        actualDataSource.setPassword("root");
        actualDataSource.setConnectionInitSql("set names utf8mb4;set names utf8;");
        RDBTracingStorageConfiguration actual = RDBTracingStorageConfiguration.getDataSourceConfiguration(actualDataSource);
        assertThat(actual.getDataSourceClassName(), is(HikariDataSource.class.getName()));
        assertThat(actual.getProps().get("driverClassName").toString(), is("org.h2.Driver"));
        assertThat(actual.getProps().get("jdbcUrl").toString(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
        assertThat(actual.getProps().get("password").toString(), is("root"));
        assertNull(actual.getProps().get("loginTimeout"));
        assertThat(actual.getProps().get("connectionInitSql"), is("set names utf8mb4;set names utf8;"));
    }
}
