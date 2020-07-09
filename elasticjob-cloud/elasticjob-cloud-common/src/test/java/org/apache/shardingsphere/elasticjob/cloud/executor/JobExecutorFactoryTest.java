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

package org.apache.shardingsphere.elasticjob.cloud.executor;

import org.apache.shardingsphere.elasticjob.cloud.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.cloud.executor.type.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestDataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.config.TestSimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.fixture.job.TestDataflowJob;
import org.apache.shardingsphere.elasticjob.cloud.executor.type.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.cloud.executor.type.SimpleJobExecutor;
import org.apache.shardingsphere.elasticjob.cloud.fixture.handler.IgnoreJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.fixture.job.OtherJob;
import org.apache.shardingsphere.elasticjob.cloud.fixture.job.TestSimpleJob;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public final class JobExecutorFactoryTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Test
    public void assertGetJobExecutorForScriptJob() {
        Mockito.when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("test.sh", IgnoreJobExceptionHandler.class));
        Assert.assertThat(JobExecutorFactory.getJobExecutor(null, jobFacade), CoreMatchers.instanceOf(ScriptJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForSimpleJob() {
        Mockito.when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        Assert.assertThat(JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade), CoreMatchers.instanceOf(SimpleJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForDataflowJob() {
        Mockito.when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(false));
        Assert.assertThat(JobExecutorFactory.getJobExecutor(new TestDataflowJob(null), jobFacade), CoreMatchers.instanceOf(DataflowJobExecutor.class));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetJobExecutorWhenJobClassWhenUnsupportedJob() {
        JobExecutorFactory.getJobExecutor(new OtherJob(), jobFacade);
    }
    
    @Test
    public void assertGetJobExecutorTwice() {
        Mockito.when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(false));
        AbstractElasticJobExecutor executor = JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade);
        AbstractElasticJobExecutor anotherExecutor = JobExecutorFactory.getJobExecutor(new TestSimpleJob(null), jobFacade);
        Assert.assertTrue(executor.hashCode() != anotherExecutor.hashCode());
    }
}
