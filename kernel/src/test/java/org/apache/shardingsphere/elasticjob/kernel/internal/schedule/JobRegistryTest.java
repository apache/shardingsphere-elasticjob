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

package org.apache.shardingsphere.elasticjob.kernel.internal.schedule;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.kernel.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JobRegistryTest {
    
    @Test
    void assertRegisterJob() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        JobRegistry.getInstance().registerJob("test_job_scheduler_for_add", jobScheduleController);
        assertThat(JobRegistry.getInstance().getJobScheduleController("test_job_scheduler_for_add"), is(jobScheduleController));
    }
    
    @Test
    void assertGetJobInstance() {
        JobRegistry.getInstance().addJobInstance("exist_job_instance", new JobInstance("127.0.0.1@-@0"));
        assertThat(JobRegistry.getInstance().getJobInstance("exist_job_instance"), is(new JobInstance("127.0.0.1@-@0")));
    }
    
    @Test
    void assertGetRegCenter() {
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_scheduler_for_add", regCenter);
        assertThat(JobRegistry.getInstance().getRegCenter("test_job_scheduler_for_add"), is(regCenter));
    }
    
    @Test
    void assertIsJobRunningIfNull() {
        assertFalse(JobRegistry.getInstance().isJobRunning("null_job_instance"));
    }
    
    @Test
    void assertIsJobRunningIfNotNull() {
        JobRegistry.getInstance().setJobRunning("exist_job_instance", true);
        assertTrue(JobRegistry.getInstance().isJobRunning("exist_job_instance"));
    }
    
    @Test
    void assertGetCurrentShardingTotalCountIfNull() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(0));
    }
    
    @Test
    void assertGetCurrentShardingTotalCountIfNotNull() {
        JobRegistry.getInstance().setCurrentShardingTotalCount("exist_job_instance", 10);
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount("exist_job_instance"), is(10));
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    @Test
    void assertShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_shutdown", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_shutdown", jobScheduleController);
        JobRegistry.getInstance().shutdown("test_job_for_shutdown");
        verify(jobScheduleController).shutdown();
        verify(regCenter).evictCacheData("/test_job_for_shutdown");
    }
    
    @Test
    void assertIsShutdownForJobSchedulerNull() {
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_scheduler_null"));
    }
    
    @Test
    void assertIsShutdownForJobInstanceNull() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_job_instance_null", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_job_instance_null", jobScheduleController);
        assertTrue(JobRegistry.getInstance().isShutdown("test_job_for_job_instance_null"));
    }
    
    @Test
    void assertIsNotShutdown() {
        JobScheduleController jobScheduleController = mock(JobScheduleController.class);
        CoordinatorRegistryCenter regCenter = mock(CoordinatorRegistryCenter.class);
        JobRegistry.getInstance().registerRegistryCenter("test_job_for_job_not_shutdown", regCenter);
        JobRegistry.getInstance().registerJob("test_job_for_job_not_shutdown", jobScheduleController);
        JobRegistry.getInstance().addJobInstance("test_job_for_job_not_shutdown", new JobInstance("127.0.0.1@-@0"));
        assertFalse(JobRegistry.getInstance().isShutdown("test_job_for_job_not_shutdown"));
    }
}
