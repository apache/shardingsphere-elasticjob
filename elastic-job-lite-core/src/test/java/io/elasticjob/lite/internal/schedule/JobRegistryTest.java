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

package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class JobRegistryTest {
    
    @Test
    public void assertRegisterJob() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerJob("test_job_scheduler_for_add", jobScheduleController, regCenter);
        assertThat(JobRegistry.getInstance().getJobScheduleController("test_job_scheduler_for_add"), is(jobScheduleController));
    }
    
    @Test
    public void assertGetJobInstance() {
        JobRegistry.getInstance().addJobInstance("exist_job_instance", new JobInstance("127.0.0.1@-@0"));
        assertThat(JobRegistry.getInstance().getJobInstance("exist_job_instance"), is(new JobInstance("127.0.0.1@-@0")));
    }
    
    @Test
    public void assertGetRegCenter() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerJob("test_job_scheduler_for_add", jobScheduleController, regCenter);
        assertThat(JobRegistry.getInstance().getRegCenter("test_job_scheduler_for_add"), is(regCenter));
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
    
    @Test
    public void assertShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerJob("test_job_for_shutdown", jobScheduleController, regCenter);
        JobRegistry.getInstance().shutdown("test_job_for_shutdown");
        verify(jobScheduleController).shutdown();
        verify(regCenter).evictCacheData("/test_job_for_shutdown");
    }
    
    @Test
    public void assertIsShutdownForJobSchedulerNull() {
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_scheduler_null"));
    }
    
    @Test
    public void assertIsShutdownForJobInstanceNull() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerJob("test_job_for_job_instance_null", jobScheduleController, regCenter);
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_instance_null"));
    }
    
    @Test
    public void assertIsNotShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerJob("test_job_for_job_not_shutdown", jobScheduleController, regCenter);
        JobRegistry.getInstance().addJobInstance("test_job_for_job_not_shutdown", new JobInstance("127.0.0.1@-@0"));
        assertFalse(JobRegistry.getInstance().isShutdown("test_job_for_job_not_shutdown"));
    }
}
