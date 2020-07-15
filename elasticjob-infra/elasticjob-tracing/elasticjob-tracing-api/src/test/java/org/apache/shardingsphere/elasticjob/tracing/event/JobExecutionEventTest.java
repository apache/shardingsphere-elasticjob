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

package org.apache.shardingsphere.elasticjob.tracing.event;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobExecutionEventTest {
    
    @Test
    public void assertNewJobExecutionEvent() {
        JobExecutionEvent actual = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getSource(), is(JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER));
        assertThat(actual.getShardingItem(), is(0));
        assertNotNull(actual.getHostname());
        assertNotNull(actual.getStartTime());
        assertNull(actual.getCompleteTime());
        assertFalse(actual.isSuccess());
        assertNull(actual.getFailureCause());
    }
    
    @Test
    public void assertExecutionSuccess() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        assertNotNull(successEvent.getCompleteTime());
        assertTrue(successEvent.isSuccess());
    }
    
    @Test
    public void assertExecutionFailure() {
        JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent failureEvent = startEvent.executionFailure("java.lang.RuntimeException: failure");
        assertNotNull(failureEvent.getCompleteTime());
        assertFalse(failureEvent.isSuccess());
        assertThat(failureEvent.getFailureCause(), is("java.lang.RuntimeException: failure"));
    }
}
