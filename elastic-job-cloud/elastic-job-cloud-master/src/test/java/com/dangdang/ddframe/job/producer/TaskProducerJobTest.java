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

package com.dangdang.ddframe.job.producer;

import com.dangdang.ddframe.job.state.ready.ReadyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class TaskProducerJobTest {
    
    @Mock
    private ReadyService readyService;
    
    private TaskProducerJob taskProducerJob;
    
    @Before
    public void setUp() {
        taskProducerJob = new TaskProducerJob();
        taskProducerJob.setJobName("test_job");
        taskProducerJob.setReadyService(readyService);
    }
    
    @Test
    public void assertExecute() throws JobExecutionException {
        taskProducerJob.execute(null);
        verify(readyService).add("test_job");
    }
}