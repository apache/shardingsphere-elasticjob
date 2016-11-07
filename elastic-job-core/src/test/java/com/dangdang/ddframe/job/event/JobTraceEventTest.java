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

package com.dangdang.ddframe.job.event;

import com.dangdang.ddframe.job.event.type.JobTraceEvent;
import com.dangdang.ddframe.job.event.type.JobTraceEvent.LogLevel;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobTraceEventTest {
    
    @Test
    public void assertGetFailureCause() {
        assertThat(new JobTraceEvent("test_job", LogLevel.INFO, "ok", new RuntimeException(new IllegalArgumentException("illegal argument"))).getFailureCause(), 
                startsWith("java.lang.RuntimeException: java.lang.IllegalArgumentException: illegal argument"));
    }
}
