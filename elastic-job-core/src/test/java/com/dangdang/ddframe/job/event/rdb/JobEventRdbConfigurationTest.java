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

import com.dangdang.ddframe.job.event.JobTraceEvent;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class JobEventRdbConfigurationTest {
    
    @Test
    public void assertCreateJobEventListenerSuccess() {
        assertThat(
                new JobEventRdbConfiguration("org.h2.Driver", "jdbc:h2:mem:job_event_storage", "sa", "", JobTraceEvent.LogLevel.INFO).createJobEventListener(), instanceOf(JobEventRdbListener.class));
    }
    
    @Test
    public void assertCreateJobEventListenerFailure() {
        assertNull(new JobEventRdbConfiguration("", "", "", "", JobTraceEvent.LogLevel.INFO).createJobEventListener());
    }
}
