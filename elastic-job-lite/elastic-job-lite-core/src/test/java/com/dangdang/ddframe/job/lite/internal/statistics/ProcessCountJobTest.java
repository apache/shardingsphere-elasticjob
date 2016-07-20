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

package com.dangdang.ddframe.job.lite.internal.statistics;

import com.dangdang.ddframe.job.api.job.dataflow.ProcessCountStatistics;
import com.dangdang.ddframe.job.lite.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.lite.fixture.TestJob;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

public final class ProcessCountJobTest {
    
    @Mock
    private ServerService serverService;
    
    private final ProcessCountJob processCountJob = new ProcessCountJob(null, 
            JobConfigurationFactory.createSimpleJobConfigurationBuilder(ProcessCountJobTest.class.getName(), 
                    TestJob.class, 3, "0/1 * * * * ?").build());
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(processCountJob, "serverService", serverService);
    }
    
    @Test
    public void assertRun() {
        ProcessCountStatistics.incrementProcessSuccessCount(ProcessCountJobTest.class.getName());
        ProcessCountStatistics.incrementProcessSuccessCount(ProcessCountJobTest.class.getName());
        ProcessCountStatistics.incrementProcessFailureCount(ProcessCountJobTest.class.getName());
        ProcessCountStatistics.incrementProcessFailureCount(ProcessCountJobTest.class.getName());
        ProcessCountStatistics.incrementProcessSuccessCount(ProcessCountJobTest.class.getName() + "_otherTestJob");
        ProcessCountStatistics.incrementProcessFailureCount(ProcessCountJobTest.class.getName() + "_otherTestJob");
        processCountJob.run();
        verify(serverService).persistProcessSuccessCount(2);
        verify(serverService).persistProcessFailureCount(2);
        assertThat(ProcessCountStatistics.getProcessSuccessCount(ProcessCountJobTest.class.getName()), is(0));
        assertThat(ProcessCountStatistics.getProcessFailureCount(ProcessCountJobTest.class.getName()), is(0));
        assertThat(ProcessCountStatistics.getProcessSuccessCount(ProcessCountJobTest.class.getName() + "_otherTestJob"), is(1));
        assertThat(ProcessCountStatistics.getProcessFailureCount(ProcessCountJobTest.class.getName() + "_otherTestJob"), is(1));
    }
}
