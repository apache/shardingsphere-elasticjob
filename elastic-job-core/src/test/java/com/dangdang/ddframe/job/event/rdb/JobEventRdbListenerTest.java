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

import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent.State;
import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.sql.SQLException;
import java.util.Collections;

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
    
    @Before
    public void setUp() throws SQLException, NoSuchFieldException {
        JobEventRdbListener jobEventRdbListener = new JobEventRdbListener(new JobEventRdbConfiguration("org.h2.Driver", "jdbc:h2:mem:job_event_storage", "sa", "", JobTraceEvent.LogLevel.INFO));
        ReflectionUtils.setFieldValue(jobEventRdbListener, "repository", repository);
        when(jobEventRdbConfiguration.createJobEventListener()).thenReturn(jobEventRdbListener);
        JobEventBus.getInstance().register(JOB_NAME, Collections.<JobEventConfiguration>singletonList(jobEventRdbConfiguration));
    }
    
    @After
    public void tearDown() {
        JobEventBus.getInstance().clearListeners(JOB_NAME);
    }
    
    @Test
    public void assertPostJobTraceEvent() {
        JobTraceEvent jobTraceEvent = new JobTraceEvent(JOB_NAME, LogLevel.INFO, "ok");
        JobEventBus.getInstance().post(jobTraceEvent);
        verify(repository, atMost(1)).addJobTraceEvent(jobTraceEvent);
    }
    
    @Test
    public void assertPostJobExecutionEvent() {
        JobExecutionEvent jobExecutionEvent = new JobExecutionEvent(JOB_NAME, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobEventBus.getInstance().post(jobExecutionEvent);
        verify(repository, atMost(1)).addJobExecutionEvent(jobExecutionEvent);
    }
    
    @Test
    public void assertPostJobStatusTraceEvent() {
        JobStatusTraceEvent jobStatusTraceEvent = new JobStatusTraceEvent(JOB_NAME, "fake_task_id", "fake_slave_id", "READY", "0", 
                State.TASK_RUNNING, "message is empty.");
        JobEventBus.getInstance().post(jobStatusTraceEvent);
        verify(repository, atMost(1)).addJobStatusTraceEvent(jobStatusTraceEvent);
    }
}
