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

package com.dangdang.ddframe.job.cloud.executor;

import com.dangdang.ddframe.job.api.JobType;
import com.dangdang.ddframe.job.cloud.executor.fixture.TestJob;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.exception.JobExecutionEnvironmentException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JobConfigurationContextTest {
    
    @Test
    public void assertSimpleJobConfigurationContext() throws JobExecutionEnvironmentException {
        assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.SIMPLE)).getTypeConfig() instanceof SimpleJobConfiguration); 
    }
    
    @Test
    public void assertDataflowJobConfigurationContext() throws JobExecutionEnvironmentException {
        assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.DATAFLOW)).getTypeConfig() instanceof DataflowJobConfiguration);
    }
    
    @Test
    public void assertScriptJobConfigurationContext() throws JobExecutionEnvironmentException {
        assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.SCRIPT)).getTypeConfig() instanceof ScriptJobConfiguration);
    }
    
    @Test
    public void assertSpringSimpleJobConfigurationContext() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        context.put("beanName", "springSimpleJobName");
        context.put("applicationContext", "applicationContext.xml");
        assertThat(new JobConfigurationContext(context).getBeanName(), is("springSimpleJobName"));
        assertThat(new JobConfigurationContext(context).getApplicationContext(), is("applicationContext.xml"));
    }
    
    @Test
    public void assertSimpleJobConfigurationContextWithExecutionType() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        assertTrue(new JobConfigurationContext(context).isTransient());
        context.put("cron", "0/1 * * * * ?");
        assertFalse(new JobConfigurationContext(context).isTransient());
    }
    
    private Map<String, String> buildJobConfigurationContextMap(final JobType jobType) {
        Map<String, String> result = new HashMap<>();
        result.put("jobName", "configuration_map_job");
        result.put("jobClass", TestJob.class.getCanonicalName());
        result.put("jobType", jobType.name());
        if (jobType == JobType.DATAFLOW) {
            result.put("streamingProcess", Boolean.TRUE.toString());
        } else if (jobType == JobType.SCRIPT) {
            result.put("scriptCommandLine", "echo test");
        }
        return result;
    }
}
