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

package com.dangdang.ddframe.job.cloud.scheduler.mesos;

import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StatisticsScheduledServiceTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private RunningService runningService;
    
    private StatisticsScheduledService statisticsScheduledService;
    
    @Before
    public void setUp() throws Exception {
        statisticsScheduledService = new StatisticsScheduledService(regCenter);
        ReflectionUtils.setFieldValue(statisticsScheduledService, "runningService", runningService);
        statisticsScheduledService.startUp();
    }
    
    @After
    public void tearDown() throws Exception {
        statisticsScheduledService.shutDown();
    }
    
    @Test
    public void assertRunOneIteration() throws Exception {
        statisticsScheduledService.runOneIteration();
        verify(runningService).getAllRunningTasks();
    }
    
    @Test
    public void assertScheduler() throws Exception {
        assertThat(statisticsScheduledService.scheduler(), instanceOf(Scheduler.class));
    }
    
    @Test
    public void assertServiceName() throws Exception {
        assertThat(statisticsScheduledService.serviceName(), is("statistics-processor"));
    }
}
