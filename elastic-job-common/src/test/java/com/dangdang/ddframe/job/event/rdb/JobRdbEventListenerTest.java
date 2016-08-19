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
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import com.dangdang.ddframe.job.event.fixture.Caller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JobRdbEventListenerTest {
    
    @Mock
    private Caller caller;
    
    private JobEventConfiguration rdbEventConfig = new JobRdbEventConfiguration(org.h2.Driver.class.getName(), "jdbc:h2:mem:job_event_bus", "sa", "", LogLevel.DEBUG);
    
    private JobEventBus jobEventBus = JobEventBus.getInstance();
    
    private String jobName = "test_rdb_event_listener";
    
    @Before
    public void setUp() {
        Map<String, JobEventConfiguration> jobEventConfigs = new LinkedHashMap<>();
        jobEventConfigs.put("rdb", rdbEventConfig);
        jobEventBus.register(jobName, jobEventConfigs.values());
    }
    
    @After
    public void tearDown() {
        jobEventBus.clearListeners(jobName);
    }
    
    @Test
    public void assertPostWithJobTraceEvent() {
        for (LogLevel each : LogLevel.values()) {
            jobEventBus.post(jobName, new JobTraceEvent(jobName, each, "ok"));
        }
    }
}
