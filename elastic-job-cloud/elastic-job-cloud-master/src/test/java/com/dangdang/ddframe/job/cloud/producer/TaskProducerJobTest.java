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

package com.dangdang.ddframe.job.cloud.producer;

import com.dangdang.ddframe.job.cloud.state.ready.ReadyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TaskProducerJobTest {
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    @Mock
    private ReadyService readyService;
    
    private TaskProducerJob taskProducerJob;
    
    @Before
    public void setUp() {
        taskProducerJob = new TaskProducerJob();
        taskProducerJob.setReadyService(readyService);
    }
    
    @Test
    public void assertExecute() throws JobExecutionException {
        when(jobExecutionContext.getJobDetail()).thenReturn(JobBuilder.newJob(TaskProducerJob.class).withIdentity("0/30 * * * * ?").build());
        TaskProducerJobContext.getInstance().put(JobKey.jobKey("0/30 * * * * ?"), "test_job");
        taskProducerJob.execute(jobExecutionContext);
        verify(readyService).add("test_job");
        TaskProducerJobContext.getInstance().remove("test_job");
    }
}
