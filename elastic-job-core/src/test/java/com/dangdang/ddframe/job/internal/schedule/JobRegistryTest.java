/**
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

package com.dangdang.ddframe.job.internal.schedule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobScheduler;

public final class JobRegistryTest {
    
    @Test
    public void assertAddJobScheduler() {
        JobScheduler jobScheduler = mock(JobScheduler.class);
        JobRegistry.getInstance().addJobScheduler("testJob_AddJobScheduler", jobScheduler);
        assertThat(JobRegistry.getInstance().getJobScheduler("testJob_AddJobScheduler"), is(jobScheduler));
    }
    
    @Test
    public void assertAddJobInstance() {
        ElasticJob elasticJob = mock(ElasticJob.class);
        JobRegistry.getInstance().addJobInstance("testJob_AddJobInstance", elasticJob);
        assertThat(JobRegistry.getInstance().getJobInstance("testJob_AddJobInstance"), is(elasticJob));
    }
}
