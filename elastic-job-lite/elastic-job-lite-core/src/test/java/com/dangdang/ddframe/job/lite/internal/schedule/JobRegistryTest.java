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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class JobRegistryTest {
    
    @Test
    public void assertAddJobScheduler() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        JobRegistry.getInstance().addJobScheduleController("test_job_scheduler_for_add", jobScheduleController);
        assertThat(JobRegistry.getInstance().getJobScheduleController("test_job_scheduler_for_add"), is(jobScheduleController));
    }
    
    @Test
    public void assertRemoveJobScheduleController() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        JobRegistry.getInstance().addJobScheduleController("test_job_scheduler_for_remove", jobScheduleController);
        assertThat(JobRegistry.getInstance().removeJobScheduleController("test_job_scheduler_for_remove"), is(jobScheduleController));
        assertNull(JobRegistry.getInstance().getJobScheduleController("test_job_scheduler_for_add"));
    }
    
    @Test
    public void assertGetJobInstanceIfNull() {
        assertThat(JobRegistry.getInstance().getJobInstance("null_job_instance"), is(new JobInstance(JobInstance.DEFAULT_INSTANCE_ID)));
    }
    
    @Test
    public void assertGetJobInstanceIfNotNull() {
        JobRegistry.getInstance().addJobInstance("exist_job_instance", new JobInstance("127.0.0.1@-@0"));
        assertThat(JobRegistry.getInstance().getJobInstance("exist_job_instance"), is(new JobInstance("127.0.0.1@-@0")));
    }
    
    @Test
    public void assertIsJobRunningIfNull() {
        assertFalse(JobRegistry.getInstance().isJobRunning("null_job_instance"));
    }
    
    @Test
    public void assertIsJobRunningIfNotNull() {
        JobRegistry.getInstance().setJobRunning("exist_job_instance", true);
        assertTrue(JobRegistry.getInstance().isJobRunning("exist_job_instance"));
    }
    
    @Test
    public void assertGetCurrentShardingTotalCountIfNull() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(0));
    }
    
    @Test
    public void assertGetCurrentShardingTotalCountIfNotNull() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("exist_job_instance", 10);
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(10));
    }
}
