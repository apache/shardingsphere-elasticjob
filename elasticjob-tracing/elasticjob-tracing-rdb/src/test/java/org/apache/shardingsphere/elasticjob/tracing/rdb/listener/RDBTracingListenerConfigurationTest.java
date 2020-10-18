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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.tracing.exception.TracingConfigurationException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class RDBTracingListenerConfigurationTest {
    
    @Test
    public void assertCreateTracingListenerSuccess() throws TracingConfigurationException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        assertThat(new RDBTracingListenerConfiguration().createTracingListener(dataSource), instanceOf(RDBTracingListener.class));
    }
    
    @Test(expected = TracingConfigurationException.class)
    public void assertCreateTracingListenerFailure() throws TracingConfigurationException {
        new RDBTracingListenerConfiguration().createTracingListener(new BasicDataSource());
    }
}
