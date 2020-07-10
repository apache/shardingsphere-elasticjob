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

import org.apache.shardingsphere.elasticjob.cloud.api.JobType;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.fixture.TestJob;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.exception.JobExecutionEnvironmentException;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JobConfigurationContextTest {
    
    @Test
    public void assertSimpleJobConfigurationContext() throws JobExecutionEnvironmentException {
        Assert.assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.SIMPLE)).getTypeConfig() instanceof SimpleJobConfiguration);
    }
    
    @Test
    public void assertDataflowJobConfigurationContext() throws JobExecutionEnvironmentException {
        Assert.assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.DATAFLOW)).getTypeConfig() instanceof DataflowJobConfiguration);
    }
    
    @Test
    public void assertScriptJobConfigurationContext() throws JobExecutionEnvironmentException {
        Assert.assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.SCRIPT)).getTypeConfig() instanceof ScriptJobConfiguration);
    }
    
    @Test
    public void assertSpringSimpleJobConfigurationContext() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        context.put("beanName", "springSimpleJobName");
        context.put("applicationContext", "applicationContext.xml");
        Assert.assertThat(new JobConfigurationContext(context).getBeanName(), Is.is("springSimpleJobName"));
        Assert.assertThat(new JobConfigurationContext(context).getApplicationContext(), Is.is("applicationContext.xml"));
    }
    
    @Test
    public void assertSimpleJobConfigurationContextWithExecutionType() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        Assert.assertTrue(new JobConfigurationContext(context).isTransient());
        context.put("cron", "0/1 * * * * ?");
        Assert.assertFalse(new JobConfigurationContext(context).isTransient());
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
