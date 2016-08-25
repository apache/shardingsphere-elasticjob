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

package com.dangdang.ddframe.job.cloud.agent.internal;

import com.dangdang.ddframe.job.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJobConfiguration;
import com.dangdang.ddframe.job.cloud.agent.fixture.TestJob;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

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
    public void assertSimpleJobConfigurationContextWithLogEvent() throws JobExecutionEnvironmentException {
        assertTrue(new JobConfigurationContext(buildJobConfigurationContextMap(JobType.SIMPLE)).getTypeConfig().getCoreConfig().getJobEventConfigs().containsKey("log"));
    }
    
    @Test
    public void assertSimpleJobConfigurationContextWithLogAndRdbEvent() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        context.put("driverClassName", "org.h2.driver");
        context.put("url", "jdbc:h2:mem:job_event_storage");
        context.put("username", "sa");
        context.put("password", "");
        context.put("logLevel", LogLevel.INFO.name());
        context.put("logEvent", "");
        Map<String, JobEventConfiguration> jobEventConfigs = new JobConfigurationContext(context).getTypeConfig().getCoreConfig().getJobEventConfigs();
        assertTrue(jobEventConfigs.containsKey("rdb"));
        assertTrue(jobEventConfigs.containsKey("log"));
    }
    
    @Test
    public void assertSimpleJobConfigurationContextWithRdbEvenWhichMissingParameters() throws JobExecutionEnvironmentException {
        Map<String, String> context = buildJobConfigurationContextMap(JobType.SIMPLE);
        context.put("driverClassName", "org.h2.driver");
        Map<String, JobEventConfiguration> jobEventConfigs = new JobConfigurationContext(context).getTypeConfig().getCoreConfig().getJobEventConfigs();
        assertTrue(!jobEventConfigs.containsKey("rdb"));
    }
    
    private Map<String, String> buildJobConfigurationContextMap(final JobType jobType) {
        Map<String, String> result = new HashMap<>();
        result.put("jobName", "configuration_map_job");
        result.put("jobClass", TestJob.class.getCanonicalName());
        result.put("jobType", jobType.name());
        if (jobType == JobType.DATAFLOW) {
            result.put("streamingProcess", Boolean.TRUE.toString());
            result.put("concurrentDataProcessThreadCount", "8");
        } else if (jobType == JobType.SCRIPT) {
            result.put("scriptCommandLine", "echo test");
        }
        return result;
    }
}
