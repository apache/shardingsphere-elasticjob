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

package io.elasticjob.cloud.scheduler.mesos;

import io.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import io.elasticjob.cloud.scheduler.context.JobContext;
import io.elasticjob.cloud.scheduler.fixture.CloudJobConfigurationBuilder;
import io.elasticjob.cloud.scheduler.state.failover.FailoverService;
import io.elasticjob.cloud.scheduler.state.ready.ReadyService;
import io.elasticjob.cloud.scheduler.state.running.RunningService;
import io.elasticjob.context.ExecutionType;
import io.elasticjob.reg.base.CoordinatorRegistryCenter;
import com.netflix.fenzo.TaskRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LaunchingTasksTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private CloudJobConfigurationService jobConfigService;
    
    @Mock
    private ReadyService readyService;
    
    @Mock
    private RunningService runningService;
    
    @Mock
    private FailoverService failoverService;
    
    private FacadeService facadeService;
    
    private LaunchingTasks launchingTasks;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        facadeService = new FacadeService(regCenter);
        ReflectionUtils.setFieldValue(facadeService, "jobConfigService", jobConfigService);
        ReflectionUtils.setFieldValue(facadeService, "readyService", readyService);
        ReflectionUtils.setFieldValue(facadeService, "runningService", runningService);
        ReflectionUtils.setFieldValue(facadeService, "failoverService", failoverService);
        when(facadeService.getEligibleJobContext()).thenReturn(Arrays.asList(
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("ready_job"), ExecutionType.READY),
                JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("failover_job"), ExecutionType.FAILOVER)));
        launchingTasks = new LaunchingTasks(facadeService.getEligibleJobContext());
    }
    
    @Test
    public void assertGetPendingTasks() {
        List<TaskRequest> actual = launchingTasks.getPendingTasks();
        assertThat(actual.size(), is(20));
    }
}
