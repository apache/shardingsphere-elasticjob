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

package org.apache.shardingsphere.elasticjob.lite.event.rdb;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.lite.context.ExecutionType;
import org.apache.shardingsphere.elasticjob.lite.event.JobEventBus;
import org.apache.shardingsphere.elasticjob.lite.event.JobEventListenerConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.event.type.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.event.type.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.lite.event.type.JobStatusTraceEvent.Source;
import org.apache.shardingsphere.elasticjob.lite.event.type.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobEventRdbListenerTest {
    
    private static final String JOB_NAME = "test_rdb_event_listener";
    
    @Mock
    private JobEventRdbConfiguration jobEventRdbConfiguration;
    
    @Mock
    private JobEventRdbStorage repository;
    
    private JobEventBus jobEventBus;
    
    @Before
    public void setUp() throws JobEventListenerConfigurationException, SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:job_event_storage");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        JobEventRdbListener jobEventRdbListener = new JobEventRdbListener(dataSource);
        ReflectionUtils.setFieldValue(jobEventRdbListener, "repository", repository);
        when(jobEventRdbConfiguration.createJobEventListener()).thenReturn(jobEventRdbListener);
        jobEventBus = new JobEventBus(jobEventRdbConfiguration);
    }
    
    @Test
    public void assertPostJobExecutionEvent() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(IpUtils.getHostName(), IpUtils.getIp(), "fake_task_id", JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        jobEventBus.post(jobExecutionEvent);
        verify(repository, atMost(1)).addJobExecutionEvent(jobExecutionEvent);
    }
    
    @Test
    public void assertPostJobStatusTraceEvent() {
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(
                JOB_NAME, "fake_task_id", "fake_slave_id", Source.LITE_EXECUTOR, ExecutionType.READY.name(), "0", State.TASK_RUNNING, "message is empty.");
        jobEventBus.post(jobStatusTraceEvent);
        verify(repository, atMost(1)).addJobStatusTraceEvent(jobStatusTraceEvent);
    }
}
