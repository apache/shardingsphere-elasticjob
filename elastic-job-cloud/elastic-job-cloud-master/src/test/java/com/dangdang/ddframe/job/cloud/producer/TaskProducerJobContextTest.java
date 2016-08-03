/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class TaskProducerJobContextTest {
    
    private JobKey jobKey = JobKey.jobKey("0/45 * * * * ?");
    
    private String jobName = "test_1";
    
    private String jobName2 = "test_2";
    
    @Test
    public void assertGetInstance() {
        assertThat(TaskProducerJobContext.getInstance(), is(TaskProducerJobContext.getInstance()));
    }
    
    @Test
    public void assertPutJobKey() throws JobExecutionException {
        TaskProducerJobContext.getInstance().put(jobKey, jobName);
        assertThat(TaskProducerJobContext.getInstance().get(jobKey).get(0), is(jobName));
        TaskProducerJobContext.getInstance().remove(jobName);
    }
    
    @Test
    public void assertPutJobWithChangedCron() throws JobExecutionException {
        TaskProducerJobContext.getInstance().put(jobKey, jobName);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        TaskProducerJobContext.getInstance().put(newJobKey, jobName);
        assertNull(TaskProducerJobContext.getInstance().get(jobKey));
        assertThat(TaskProducerJobContext.getInstance().get(newJobKey).get(0), is(jobName));
        TaskProducerJobContext.getInstance().remove(jobName);
    }
    
    @Test
    public void assertPutMoreJobWithChangedCron() throws JobExecutionException {
        TaskProducerJobContext.getInstance().put(jobKey, jobName);
        TaskProducerJobContext.getInstance().put(jobKey, jobName2);
        JobKey newJobKey = JobKey.jobKey("0/15 * * * * ?");
        TaskProducerJobContext.getInstance().put(newJobKey, jobName);
        assertThat(TaskProducerJobContext.getInstance().get(jobKey).get(0), is(jobName2));
        assertThat(TaskProducerJobContext.getInstance().get(newJobKey).get(0), is(jobName));
        TaskProducerJobContext.getInstance().remove(jobName);
        TaskProducerJobContext.getInstance().remove(jobName2);
    }
    
    @Test
    public void assertRemoveJobKey() throws JobExecutionException {
        TaskProducerJobContext.getInstance().put(jobKey, jobName);
        TaskProducerJobContext.getInstance().remove(jobName);
        assertNull(TaskProducerJobContext.getInstance().get(jobKey));
    }
}
