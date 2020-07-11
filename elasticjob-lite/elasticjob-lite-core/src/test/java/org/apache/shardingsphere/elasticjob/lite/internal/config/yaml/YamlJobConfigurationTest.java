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

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.common.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlJobConfigurationTest {
    
    private static final String YAML = "cron: 0/1 * * * * ?\n"
            + "description: Job description\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobErrorHandlerType: IGNORE\n"
            + "jobExecutorServiceHandlerType: CPU\n"
            + "jobName: test_job\n"
            + "jobParameter: param\n"
            + "jobShardingStrategyType: AVG_ALLOCATION\n"
            + "maxTimeDiffSeconds: 0\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "props:\n"
            + "  key: value\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingItemParameters: 0=A,1=B,2=C\n"
            + "shardingTotalCount: 3\n";
    
    private static final String YAML_WITH_NULL = "cron: 0/1 * * * * ?\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobName: test_job\n"
            + "maxTimeDiffSeconds: 0\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingTotalCount: 3\n";
    
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
    
    @Test
    public void assertMarshal() {
        YamlJobConfiguration yamlJobConfiguration = new YamlJobConfiguration();
        yamlJobConfiguration.setJobName("test_job");
        yamlJobConfiguration.setCron("0/1 * * * * ?");
        yamlJobConfiguration.setShardingTotalCount(3);
        yamlJobConfiguration.setShardingItemParameters("0=A,1=B,2=C");
        yamlJobConfiguration.setJobParameter("param");
        yamlJobConfiguration.setJobShardingStrategyType("AVG_ALLOCATION");
        yamlJobConfiguration.setJobExecutorServiceHandlerType("CPU");
        yamlJobConfiguration.setJobErrorHandlerType("IGNORE");
        yamlJobConfiguration.setDescription("Job description");
        yamlJobConfiguration.getProps().setProperty("key", "value");
        assertThat(YamlEngine.marshal(yamlJobConfiguration), is(YAML));
    }
    
    @Test
    public void assertMarshalWithNullValue() {
        YamlJobConfiguration yamlJobConfiguration = new YamlJobConfiguration();
        yamlJobConfiguration.setJobName("test_job");
        yamlJobConfiguration.setCron("0/1 * * * * ?");
        yamlJobConfiguration.setShardingTotalCount(3);
        assertThat(YamlEngine.marshal(yamlJobConfiguration), is(YAML_WITH_NULL));
    }
    
    @Test
    public void assertUnmarshal() {
        YamlJobConfiguration yamlJobConfiguration = YamlEngine.unmarshal(YAML, YamlJobConfiguration.class);
        assertThat(yamlJobConfiguration.getJobName(), is("test_job"));
        assertThat(yamlJobConfiguration.getCron(), is("0/1 * * * * ?"));
        assertThat(yamlJobConfiguration.getShardingTotalCount(), is(3));
        assertThat(yamlJobConfiguration.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(yamlJobConfiguration.getJobParameter(), is("param"));
        assertFalse(yamlJobConfiguration.isMonitorExecution());
        assertFalse(yamlJobConfiguration.isFailover());
        assertFalse(yamlJobConfiguration.isMisfire());
        assertThat(yamlJobConfiguration.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(yamlJobConfiguration.getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(yamlJobConfiguration.getJobErrorHandlerType(), is("IGNORE"));
        assertThat(yamlJobConfiguration.getDescription(), is("Job description"));
        assertThat(yamlJobConfiguration.getProps().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertUnmarshalWithNullValue() {
        YamlJobConfiguration yamlJobConfiguration = YamlEngine.unmarshal(YAML_WITH_NULL, YamlJobConfiguration.class);
        assertThat(yamlJobConfiguration.getJobName(), is("test_job"));
        assertThat(yamlJobConfiguration.getCron(), is("0/1 * * * * ?"));
        assertThat(yamlJobConfiguration.getShardingTotalCount(), is(3));
        assertNull(yamlJobConfiguration.getShardingItemParameters());
        assertNull(yamlJobConfiguration.getJobParameter());
        assertFalse(yamlJobConfiguration.isMonitorExecution());
        assertFalse(yamlJobConfiguration.isFailover());
        assertFalse(yamlJobConfiguration.isMisfire());
        assertNull(yamlJobConfiguration.getJobShardingStrategyType());
        assertNull(yamlJobConfiguration.getJobExecutorServiceHandlerType());
        assertNull(yamlJobConfiguration.getJobErrorHandlerType());
        assertNull(yamlJobConfiguration.getDescription());
        assertTrue(yamlJobConfiguration.getProps().isEmpty());
        assertFalse(yamlJobConfiguration.isDisabled());
        assertFalse(yamlJobConfiguration.isOverwrite());
    }
}
