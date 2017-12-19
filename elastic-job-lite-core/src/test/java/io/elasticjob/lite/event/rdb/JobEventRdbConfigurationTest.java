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

package io.elasticjob.lite.event.rdb;

import io.elasticjob.lite.event.JobEventListenerConfigurationException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class JobEventRdbConfigurationTest {
    
    @Test
    public void assertGetDataSource() throws JobEventListenerConfigurationException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        assertThat((BasicDataSource) (new JobEventRdbConfiguration(dataSource).getDataSource()), is(dataSource));
    }
    
    @Test
    public void assertCreateJobEventListenerSuccess() throws JobEventListenerConfigurationException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        assertThat(new JobEventRdbConfiguration(dataSource).createJobEventListener(), instanceOf(JobEventRdbListener.class));
    }
    
    @Test(expected = JobEventListenerConfigurationException.class)
    public void assertCreateJobEventListenerFailure() throws JobEventListenerConfigurationException {
        new JobEventRdbConfiguration(new BasicDataSource()).createJobEventListener();
    }
}
