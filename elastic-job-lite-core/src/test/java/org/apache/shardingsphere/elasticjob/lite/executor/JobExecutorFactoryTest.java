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

package org.apache.shardingsphere.elasticjob.lite.executor;

import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.executor.type.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.SimpleJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.fixture.config.TestDataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.config.TestScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.config.TestSimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.handler.IgnoreJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.OtherJob;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.TestDataflowJob;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.TestSimpleJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobExecutorFactoryTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Test
    public void assertGetJobExecutorForScriptJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("test.sh", IgnoreJobExceptionHandler.class));
        assertThat(JobExecutorFactory.getJobExecutor(null, jobFacade), instanceOf(ScriptJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForSimpleJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        assertThat(JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade), instanceOf(SimpleJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForDataflowJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(false));
        assertThat(JobExecutorFactory.getJobExecutor(new TestDataflowJob(null), jobFacade), instanceOf(DataflowJobExecutor.class));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetJobExecutorWhenJobClassWhenUnsupportedJob() {
        JobExecutorFactory.getJobExecutor(new OtherJob(), jobFacade);
    }
    
    @Test
    public void assertGetJobExecutorTwice() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(false));
        ElasticJobExecutor executor = JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade);
        ElasticJobExecutor anotherExecutor = JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade);
        assertTrue(executor.hashCode() != anotherExecutor.hashCode());
    }
}
