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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class TransientProducerJobContextTest {
    
    private final JobKey jobKey = JobKey.jobKey("0/45 * * * * ?");
    
    private final String jobName = "test_job";
    
    @Test
    public void assertGetInstance() {
        assertThat(TransientJobRegistry.getInstance(), is(TransientJobRegistry.getInstance()));
    }
    
    @Test
    public void assertPutJobKey() throws JobExecutionException {
        TransientJobRegistry.getInstance().put(jobKey, jobName);
        assertThat(TransientJobRegistry.getInstance().get(jobKey).get(0), is(jobName));
        TransientJobRegistry.getInstance().remove(jobName);
    }
    
    @Test
    public void assertPutJobWithChangedCron() throws JobExecutionException {
        TransientJobRegistry.getInstance().put(jobKey, jobName);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        TransientJobRegistry.getInstance().put(newJobKey, jobName);
        assertTrue(TransientJobRegistry.getInstance().get(jobKey).isEmpty());
        assertThat(TransientJobRegistry.getInstance().get(newJobKey).get(0), is(jobName));
        TransientJobRegistry.getInstance().remove(jobName);
    }
    
    @Test
    public void assertPutMoreJobWithChangedCron() throws JobExecutionException {
        String jobName2 = "other_test_job";
        TransientJobRegistry.getInstance().put(jobKey, jobName);
        TransientJobRegistry.getInstance().put(jobKey, jobName2);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        TransientJobRegistry.getInstance().put(newJobKey, jobName);
        assertThat(TransientJobRegistry.getInstance().get(jobKey).get(0), is(jobName2));
        assertThat(TransientJobRegistry.getInstance().get(newJobKey).get(0), is(jobName));
        TransientJobRegistry.getInstance().remove(jobName);
        TransientJobRegistry.getInstance().remove(jobName2);
    }
    
    @Test
    public void assertRemoveJobKey() throws JobExecutionException {
        TransientJobRegistry.getInstance().put(jobKey, jobName);
        TransientJobRegistry.getInstance().remove(jobName);
        assertTrue(TransientJobRegistry.getInstance().get(jobKey).isEmpty());
    }
    
    @Test
    public void assertContainsKey() {
        TransientJobRegistry.getInstance().put(jobKey, jobName);
        assertTrue(TransientJobRegistry.getInstance().containsKey(jobKey));
        TransientJobRegistry.getInstance().remove(jobName);
        assertFalse(TransientJobRegistry.getInstance().containsKey(jobKey));
    }
}
