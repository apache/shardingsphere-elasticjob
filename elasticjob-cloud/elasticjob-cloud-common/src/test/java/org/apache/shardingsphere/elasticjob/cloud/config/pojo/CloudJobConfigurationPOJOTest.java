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

package org.apache.shardingsphere.elasticjob.cloud.config.pojo;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CloudJobConfigurationPOJOTest {
    
    private static final String YAML = "appName: app\n"
            + "cpuCount: 1.0\n"
            + "cron: 0/1 * * * * ?\n"
            + "description: Job description\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobErrorHandlerType: IGNORE\n"
            + "jobExecutionType: DAEMON\n"
            + "jobExecutorServiceHandlerType: CPU\n"
            + "jobName: test_job\n"
            + "jobParameter: param\n"
            + "jobShardingStrategyType: AVG_ALLOCATION\n"
            + "maxTimeDiffSeconds: 0\n"
            + "memoryMB: 128.0\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "props:\n"
            + "  key: value\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingItemParameters: 0=A,1=B,2=C\n"
            + "shardingTotalCount: 3\n";
    
    private static final String YAML_WITH_NULL = "appName: app\n"
            + "cpuCount: 1.0\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobExecutionType: DAEMON\n"
            + "jobName: test_job\n"
            + "maxTimeDiffSeconds: 0\n"
            + "memoryMB: 128.0\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "reconcileIntervalMinutes: 0\n"
            + "shardingTotalCount: 3\n";
    
    @Test
    public void assertToJobConfiguration() {
        CloudJobConfigurationPOJO pojo = new CloudJobConfigurationPOJO();
        pojo.setAppName("app");
        pojo.setCpuCount(1d);
        pojo.setMemoryMB(128d);
        pojo.setJobExecutionType(CloudJobExecutionType.DAEMON);
        pojo.setJobName("test_job");
        pojo.setCron("0/1 * * * * ?");
        pojo.setShardingTotalCount(3);
        pojo.setShardingItemParameters("0=A,1=B,2=C");
        pojo.setJobParameter("param");
        pojo.setMonitorExecution(true);
        pojo.setFailover(true);
        pojo.setMisfire(true);
        pojo.setJobShardingStrategyType("AVG_ALLOCATION");
        pojo.setJobExecutorServiceHandlerType("CPU");
        pojo.setJobErrorHandlerType("IGNORE");
        pojo.setDescription("Job description");
        pojo.getProps().setProperty("key", "value");
        pojo.setDisabled(true);
        pojo.setOverwrite(true);
        CloudJobConfiguration actual = pojo.toCloudJobConfiguration();
        assertThat(actual.getAppName(), is("app"));
        assertThat(actual.getCpuCount(), is(1d));
        assertThat(actual.getMemoryMB(), is(128d));
        assertThat(actual.getJobExecutionType(), is(CloudJobExecutionType.DAEMON));
        assertThat(actual.getJobConfig().getJobName(), is("test_job"));
        assertThat(actual.getJobConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getJobConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getJobConfig().getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(actual.getJobConfig().getJobParameter(), is("param"));
        assertTrue(actual.getJobConfig().isMonitorExecution());
        assertTrue(actual.getJobConfig().isFailover());
        assertTrue(actual.getJobConfig().isMisfire());
        assertThat(actual.getJobConfig().getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getJobConfig().getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(actual.getJobConfig().getJobErrorHandlerType(), is("IGNORE"));
        assertThat(actual.getJobConfig().getDescription(), is("Job description"));
        assertThat(actual.getJobConfig().getProps().getProperty("key"), is("value"));
        assertTrue(actual.getJobConfig().isDisabled());
        assertTrue(actual.getJobConfig().isOverwrite());
    }
    
    @Test
    public void assertFromJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?")
                .shardingItemParameters("0=A,1=B,2=C").jobParameter("param")
                .monitorExecution(true).failover(true).misfire(true)
                .jobShardingStrategyType("AVG_ALLOCATION").jobExecutorServiceHandlerType("CPU").jobErrorHandlerType("IGNORE")
                .description("Job description").setProperty("key", "value")
                .disabled(true).overwrite(true).build();
        CloudJobConfiguration cloudJobConfig = new CloudJobConfiguration("app", 1d, 128d, CloudJobExecutionType.DAEMON, jobConfig);
        CloudJobConfigurationPOJO actual = CloudJobConfigurationPOJO.fromCloudJobConfiguration(cloudJobConfig);
        assertThat(actual.getAppName(), is("app"));
        assertThat(actual.getCpuCount(), is(1d));
        assertThat(actual.getMemoryMB(), is(128d));
        assertThat(actual.getJobExecutionType(), is(CloudJobExecutionType.DAEMON));
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
        CloudJobConfigurationPOJO actual = new CloudJobConfigurationPOJO();
        actual.setAppName("app");
        actual.setCpuCount(1d);
        actual.setMemoryMB(128d);
        actual.setJobExecutionType(CloudJobExecutionType.DAEMON);
        actual.setJobName("test_job");
        actual.setCron("0/1 * * * * ?");
        actual.setShardingTotalCount(3);
        actual.setShardingItemParameters("0=A,1=B,2=C");
        actual.setJobParameter("param");
        actual.setJobShardingStrategyType("AVG_ALLOCATION");
        actual.setJobExecutorServiceHandlerType("CPU");
        actual.setJobErrorHandlerType("IGNORE");
        actual.setDescription("Job description");
        actual.getProps().setProperty("key", "value");
        assertThat(YamlEngine.marshal(actual), is(YAML));
    }
    
    @Test
    public void assertMarshalWithNullValue() {
        CloudJobConfigurationPOJO actual = new CloudJobConfigurationPOJO();
        actual.setAppName("app");
        actual.setCpuCount(1d);
        actual.setMemoryMB(128d);
        actual.setJobExecutionType(CloudJobExecutionType.DAEMON);
        actual.setJobName("test_job");
        actual.setShardingTotalCount(3);
        assertThat(YamlEngine.marshal(actual), is(YAML_WITH_NULL));
    }
    
    @Test
    public void assertUnmarshal() {
        CloudJobConfigurationPOJO actual = YamlEngine.unmarshal(YAML, CloudJobConfigurationPOJO.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(actual.getJobParameter(), is("param"));
        assertFalse(actual.isMonitorExecution());
        assertFalse(actual.isFailover());
        assertFalse(actual.isMisfire());
        assertThat(actual.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getJobExecutorServiceHandlerType(), is("CPU"));
        assertThat(actual.getJobErrorHandlerType(), is("IGNORE"));
        assertThat(actual.getDescription(), is("Job description"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertUnmarshalWithNullValue() {
        CloudJobConfigurationPOJO actual = YamlEngine.unmarshal(YAML_WITH_NULL, CloudJobConfigurationPOJO.class);
        assertThat(actual.getAppName(), is("app"));
        assertThat(actual.getCpuCount(), is(1d));
        assertThat(actual.getMemoryMB(), is(128d));
        assertThat(actual.getJobExecutionType(), is(CloudJobExecutionType.DAEMON));
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertNull(actual.getShardingItemParameters());
        assertNull(actual.getJobParameter());
        assertFalse(actual.isMonitorExecution());
        assertFalse(actual.isFailover());
        assertFalse(actual.isMisfire());
        assertNull(actual.getJobShardingStrategyType());
        assertNull(actual.getJobExecutorServiceHandlerType());
        assertNull(actual.getJobErrorHandlerType());
        assertNull(actual.getDescription());
        assertTrue(actual.getProps().isEmpty());
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
    }
}
