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

import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public final class DataSourceFactoryTest {
    
    @Test
    public void assertGetDataSource() {
        DataSource actual = DataSourceFactory.getDataSource("mockDriverClass", "mockUrl", "root", "root");
        assertThat(actual, is(DataSourceFactory.getDataSource("mockDriverClass", "mockUrl", "root", "root")));
        assertThat(actual, not(DataSourceFactory.getDataSource("mockDriverClass", "otherMockUrl", "root", "root")));
    }
}
