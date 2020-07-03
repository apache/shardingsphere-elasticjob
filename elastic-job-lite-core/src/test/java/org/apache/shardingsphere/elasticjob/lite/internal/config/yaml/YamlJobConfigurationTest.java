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

package org.apache.shardingsphere.elasticjob.lite.internal.config.yaml;

import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlJobConfigurationTest {
    
    @Test
    public void assertToJobConfiguration() {
        YamlJobConfiguration yamlJobConfiguration = new YamlJobConfiguration();
        yamlJobConfiguration.setJobName("test_job");
        yamlJobConfiguration.setCron("0/1 * * * * ?");
        yamlJobConfiguration.setShardingTotalCount(3);
        yamlJobConfiguration.setShardingItemParameters("0=A,1=B,2=C");
        yamlJobConfiguration.setJobParameter("param");
        yamlJobConfiguration.setMonitorExecution(true);
        yamlJobConfiguration.setFailover(true);
        yamlJobConfiguration.setMisfire(true);
        yamlJobConfiguration.setJobShardingStrategyType("AVG_ALLOCATION");
        yamlJobConfiguration.setJobExecutorServiceHandlerType("CPU");
        yamlJobConfiguration.setJobErrorHandlerType("IGNORE");
        yamlJobConfiguration.setDescription("Job description");
        yamlJobConfiguration.getProps().setProperty("key", "value");
        yamlJobConfiguration.setDisabled(true);
        yamlJobConfiguration.setOverwrite(true);
        JobConfiguration actual = yamlJobConfiguration.toJobConfiguration();
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(actual.getJobParameter(), is("param"));
        assertTrue(actual.isMonitorExecution());
        assertTrue(actual.isFailover());
        assertTrue(actual.isMisfire());
        assertThat(actual.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(actual.getJobErrorHandlerType(), is("IGNORE"));
        assertThat(actual.getDescription(), is("Job description"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertFromJobConfiguration() {
        JobConfiguration jobConfiguration = JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?")
                .shardingItemParameters("0=A,1=B,2=C").jobParameter("param")
                .monitorExecution(true).failover(true).misfire(true)
                .jobShardingStrategyType("AVG_ALLOCATION").jobExecutorServiceHandlerType("CPU").jobErrorHandlerType("IGNORE")
                .description("Job description").setProperty("key", "value")
                .disabled(true).overwrite(true).build();
        YamlJobConfiguration actual = YamlJobConfiguration.fromJobConfiguration(jobConfiguration);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(actual.getJobParameter(), is("param"));
        assertTrue(actual.isMonitorExecution());
        assertTrue(actual.isFailover());
        assertTrue(actual.isMisfire());
        assertThat(actual.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(actual.getJobErrorHandlerType(), is("IGNORE"));
        assertThat(actual.getDescription(), is("Job description"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
}
