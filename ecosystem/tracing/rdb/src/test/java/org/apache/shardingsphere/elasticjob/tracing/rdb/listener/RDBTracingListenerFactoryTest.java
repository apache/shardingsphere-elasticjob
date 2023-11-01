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

package org.apache.shardingsphere.elasticjob.tracing.rdb.listener;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.spi.tracing.exception.TracingConfigurationException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RDBTracingListenerFactoryTest {
    
    @Test
    void assertCreateTracingListenerSuccess() throws TracingConfigurationException {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        assertThat(new RDBTracingListenerFactory().create(dataSource), instanceOf(RDBTracingListener.class));
    }
    
    @Test
    void assertCreateTracingListenerFailure() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException());
        assertThrows(TracingConfigurationException.class, () -> new RDBTracingListenerFactory().create(dataSource));
    }
}
