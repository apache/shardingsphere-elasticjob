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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.producer;

import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProducerJobTest {
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    @Mock
    private ReadyService readyService;
    
    private final TransientProducerRepository repository = new TransientProducerRepository();
    
    private TransientProducerScheduler.ProducerJob producerJob;
    
    @Before
    public void setUp() {
        producerJob = new TransientProducerScheduler.ProducerJob();
        producerJob.setRepository(repository);
        producerJob.setReadyService(readyService);
    }
    
    @Test
    public void assertExecute() {
        when(jobExecutionContext.getJobDetail()).thenReturn(JobBuilder.newJob(TransientProducerScheduler.ProducerJob.class).withIdentity("0/30 * * * * ?").build());
        repository.put(JobKey.jobKey("0/30 * * * * ?"), "test_job");
        producerJob.execute(jobExecutionContext);
        verify(readyService).addTransient("test_job");
        repository.remove("test_job");
    }
}
