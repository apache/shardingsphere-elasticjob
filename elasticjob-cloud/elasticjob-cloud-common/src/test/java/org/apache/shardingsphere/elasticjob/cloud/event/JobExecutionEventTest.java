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

package org.apache.shardingsphere.elasticjob.cloud.event;

import org.apache.shardingsphere.elasticjob.cloud.event.type.JobExecutionEvent;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public final class JobExecutionEventTest {
    
    @Test
    public void assertNewJobExecutionEvent() {
        JobExecutionEvent actual = new JobExecutionEvent("fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getSource(), Is.is(JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER));
        Assert.assertThat(actual.getShardingItem(), Is.is(0));
        Assert.assertNotNull(actual.getHostname());
        Assert.assertNotNull(actual.getStartTime());
        Assert.assertNull(actual.getCompleteTime());
        Assert.assertFalse(actual.isSuccess());
        Assert.assertThat(actual.getFailureCause(), Is.is(""));
    }
    
    @Test
    public void assertExecutionSuccess() {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent successEvent = startEvent.executionSuccess();
        Assert.assertNotNull(successEvent.getCompleteTime());
        Assert.assertTrue(successEvent.isSuccess());
    }
    
    @Test
    public void assertExecutionFailure() {
        JobExecutionEvent startEvent = new JobExecutionEvent("fake_task_id", "test_job", JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
        JobExecutionEvent failureEvent = startEvent.executionFailure(new RuntimeException("failure"));
        Assert.assertNotNull(failureEvent.getCompleteTime());
        Assert.assertFalse(failureEvent.isSuccess());
        Assert.assertThat(failureEvent.getFailureCause(), CoreMatchers.startsWith("java.lang.RuntimeException: failure"));
    }
}
