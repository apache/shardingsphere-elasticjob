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

package org.apache.shardingsphere.elasticjob.cloud.event.rdb;

import org.apache.shardingsphere.elasticjob.cloud.event.JobEventListenerConfigurationException;
import org.apache.commons.dbcp.BasicDataSource;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public final class JobEventRdbConfigurationTest {
    
    @Test
    public void assertGetDataSource() throws JobEventListenerConfigurationException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Assert.assertThat((BasicDataSource) (new JobEventRdbConfiguration(dataSource).getDataSource()), Is.is(dataSource));
    }
    
    @Test
    public void assertCreateJobEventListenerSuccess() throws JobEventListenerConfigurationException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Assert.assertThat(new JobEventRdbConfiguration(dataSource).createJobEventListener(), CoreMatchers.instanceOf(JobEventRdbListener.class));
    }
    
    @Test(expected = JobEventListenerConfigurationException.class)
    public void assertCreateJobEventListenerFailure() throws JobEventListenerConfigurationException {
        new JobEventRdbConfiguration(new BasicDataSource()).createJobEventListener();
    }
}
