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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.api.exception.JobConfigurationException;
import com.dangdang.ddframe.job.api.executor.JobFacade;
import com.dangdang.ddframe.job.api.fixture.config.TestDataflowJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.config.TestScriptJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.config.TestSimpleJobConfiguration;
import com.dangdang.ddframe.job.api.fixture.job.OtherJob;
import com.dangdang.ddframe.job.api.fixture.job.TestDataflowJob;
import com.dangdang.ddframe.job.api.fixture.job.TestSimpleJob;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.dataflow.executor.DataflowJobExecutor;
import com.dangdang.ddframe.job.api.type.script.executor.ScriptJobExecutor;
import com.dangdang.ddframe.job.api.type.simple.executor.SimpleJobExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobExecutorFactoryTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Test
    public void assertGetJobExecutorForScriptJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("test.sh"));
        assertThat(JobExecutorFactory.getJobExecutor(null, jobFacade), instanceOf(ScriptJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForSimpleJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        assertThat(JobExecutorFactory.getJobExecutor(new TestSimpleJob(), jobFacade), instanceOf(SimpleJobExecutor.class));
    }
    
    @Test
    public void assertGetJobExecutorForDataflowJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(DataflowJobConfiguration.DataflowType.SEQUENCE, false, 1));
        assertThat(JobExecutorFactory.getJobExecutor(new TestDataflowJob(), jobFacade), instanceOf(DataflowJobExecutor.class));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetJobExecutorWhenJobClassWhenUnsupportedJob() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration());
        JobExecutorFactory.getJobExecutor(new OtherJob(), jobFacade);
    }
}
