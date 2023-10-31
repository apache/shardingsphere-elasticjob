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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSourceRegistryTest {
    
    @Mock
    private RDBTracingStorageConfiguration dataSourceConfig;
    
    @Test
    void assertGetDataSourceBySameConfiguration() {
        when(dataSourceConfig.createDataSource()).then(invocation -> mock(DataSource.class));
        DataSource expected = DataSourceRegistry.getInstance().getDataSource(dataSourceConfig);
        DataSource actual = DataSourceRegistry.getInstance().getDataSource(dataSourceConfig);
        verify(dataSourceConfig).createDataSource();
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetDataSourceWithDifferentConfiguration() {
        when(dataSourceConfig.createDataSource()).then(invocation -> mock(DataSource.class));
        RDBTracingStorageConfiguration anotherDataSourceConfig = mock(RDBTracingStorageConfiguration.class);
        when(anotherDataSourceConfig.createDataSource()).then(invocation -> mock(DataSource.class));
        DataSource one = DataSourceRegistry.getInstance().getDataSource(dataSourceConfig);
        DataSource another = DataSourceRegistry.getInstance().getDataSource(anotherDataSourceConfig);
        verify(dataSourceConfig).createDataSource();
        verify(anotherDataSourceConfig).createDataSource();
        assertThat(another, not(one));
    }
}
