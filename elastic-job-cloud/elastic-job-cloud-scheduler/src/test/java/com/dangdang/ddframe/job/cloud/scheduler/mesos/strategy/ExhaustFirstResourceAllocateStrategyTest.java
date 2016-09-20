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

package com.dangdang.ddframe.job.cloud.scheduler.mesos.strategy;

import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.ExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.context.JobContext;
import com.dangdang.ddframe.job.cloud.scheduler.context.TaskContext;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.HardwareResource;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.facade.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.scheduler.state.fixture.CloudJobConfigurationBuilder;
import org.apache.mesos.Protos;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExhaustFirstResourceAllocateStrategyTest {
    
    @Test
    public void assertAllocateForTransient() {
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(
                Arrays.asList(new HardwareResource(OfferBuilder.createOffer("offer_0", 8d, 1280d)), new HardwareResource(OfferBuilder.createOffer("offer_1", 8d, 1280d))), null);
        List<Protos.TaskInfo> actual = resourceAllocateStrategy.allocate(Arrays.asList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_0"), ExecutionType.READY),
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1"), ExecutionType.READY)));
        assertThat(actual.size(), is(10));
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getTaskId().getValue(), startsWith("test_job_0@-@" + i + "@-@READY@-@"));
            if (i < 8) {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_0"));
            } else {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_1"));
            }
        }
    }
    
    @Test
    public void assertAllocateForDaemon() {
        FacadeService facadeService = mock(FacadeService.class);
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(
                Arrays.asList(new HardwareResource(OfferBuilder.createOffer("offer_0", 6d, 1280d)), new HardwareResource(OfferBuilder.createOffer("offer_1", 6d, 1280d))), facadeService);
        when(facadeService.getRunningTasks("test_job_0")).thenReturn(
                Arrays.asList(TaskContext.from("test_job_0@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("test_job_0@-@9@-@READY@-@SLAVE-S0@-@UUID")));
        when(facadeService.getRunningTasks("test_job_1")).thenReturn(
                Arrays.asList(TaskContext.from("test_job_1@-@0@-@READY@-@SLAVE-S0@-@UUID"), TaskContext.from("test_job_1@-@9@-@READY@-@SLAVE-S0@-@UUID")));
        List<Protos.TaskInfo> actual = resourceAllocateStrategy.allocate(Arrays.asList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_0", JobExecutionType.DAEMON), ExecutionType.READY),
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1", JobExecutionType.DAEMON), ExecutionType.READY)));
        assertThat(actual.size(), is(8));
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getTaskId().getValue(), startsWith("test_job_0@-@" + (i + 1) + "@-@READY@-@"));
            if (i < 6) {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_0"));
            } else {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_1"));
            }
        }
    }
}
