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

package org.apache.shardingsphere.elasticjob.infra.pojo;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobConfigurationPOJOTest {
    
    private static final String YAML = "cron: 0/1 * * * * ?\n"
            + "dagRunAlone: false\n"
            + "dagSkipWhenFail: false\n"
            + "description: Job description\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobErrorHandlerType: IGNORE\n"
            + "jobExecutorServiceHandlerType: CPU\n"
            + "jobName: test_job\n"
            + "jobParameter: param\n"
            + "jobShardingStrategyType: AVG_ALLOCATION\n"
            + "maxTimeDiffSeconds: -1\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "props:\n"
            + "  key: value\n"
            + "reconcileIntervalMinutes: 0\n"
            + "retryInterval: 0\n"
            + "retryTimes: 0\n"
            + "shardingItemParameters: 0=A,1=B,2=C\n"
            + "shardingTotalCount: 3\n";

    private static final String DAG_YAML = "cron: 0/1 * * * * ?\n"
            + "dagDependencies: jobA,jobB\n"
            + "dagName: fooDag\n"
            + "dagRunAlone: false\n"
            + "dagSkipWhenFail: true\n"
            + "description: Job description\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobErrorHandlerType: IGNORE\n"
            + "jobExecutorServiceHandlerType: CPU\n"
            + "jobName: test_job\n"
            + "jobParameter: param\n"
            + "jobShardingStrategyType: AVG_ALLOCATION\n"
            + "maxTimeDiffSeconds: -1\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "props:\n"
            + "  key: value\n"
            + "reconcileIntervalMinutes: 0\n"
            + "retryInterval: 500\n"
            + "retryTimes: 3\n"
            + "shardingItemParameters: 0=A,1=B,2=C\n"
            + "shardingTotalCount: 3\n";

    private static final String YAML_WITH_NULL = "cron: 0/1 * * * * ?\n"
            + "dagRunAlone: false\n"
            + "dagSkipWhenFail: false\n"
            + "disabled: false\n"
            + "failover: false\n"
            + "jobName: test_job\n"
            + "maxTimeDiffSeconds: -1\n"
            + "misfire: false\n"
            + "monitorExecution: false\n"
            + "overwrite: false\n"
            + "reconcileIntervalMinutes: 0\n"
            + "retryInterval: 0\n"
            + "retryTimes: 0\n"
            + "shardingTotalCount: 3\n";
    
    @Test
    public void assertToJobConfiguration() {
        JobConfigurationPOJO pojo = new JobConfigurationPOJO();
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
        JobConfiguration actual = pojo.toJobConfiguration();
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
        JobConfigurationPOJO actual = JobConfigurationPOJO.fromJobConfiguration(jobConfiguration);
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
        JobConfigurationPOJO actual = new JobConfigurationPOJO();
        actual.setJobName("test_job");
        actual.setCron("0/1 * * * * ?");
        actual.setShardingTotalCount(3);
        actual.setShardingItemParameters("0=A,1=B,2=C");
        actual.setJobParameter("param");
        actual.setMaxTimeDiffSeconds(-1);
        actual.setJobShardingStrategyType("AVG_ALLOCATION");
        actual.setJobExecutorServiceHandlerType("CPU");
        actual.setJobErrorHandlerType("IGNORE");
        actual.setDescription("Job description");
        actual.getProps().setProperty("key", "value");
        assertThat(YamlEngine.marshal(actual), is(YAML));
    }
    
    @Test
    public void assertMarshalWithNullValue() {
        JobConfigurationPOJO actual = new JobConfigurationPOJO();
        actual.setJobName("test_job");
        actual.setCron("0/1 * * * * ?");
        actual.setShardingTotalCount(3);
        actual.setMaxTimeDiffSeconds(-1);
        assertThat(YamlEngine.marshal(actual), is(YAML_WITH_NULL));
    }
    
    @Test
    public void assertUnmarshal() {
        JobConfigurationPOJO actual = YamlEngine.unmarshal(YAML, JobConfigurationPOJO.class);
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
        JobConfigurationPOJO actual = YamlEngine.unmarshal(YAML_WITH_NULL, JobConfigurationPOJO.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
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

    @Test
    public void assertMarshalWithDag() {
        JobConfigurationPOJO actual = new JobConfigurationPOJO();
        actual.setJobName("test_job");
        actual.setCron("0/1 * * * * ?");
        actual.setShardingTotalCount(3);
        actual.setShardingItemParameters("0=A,1=B,2=C");
        actual.setJobParameter("param");
        actual.setMaxTimeDiffSeconds(-1);
        actual.setJobShardingStrategyType("AVG_ALLOCATION");
        actual.setJobExecutorServiceHandlerType("CPU");
        actual.setJobErrorHandlerType("IGNORE");
        actual.setDescription("Job description");
        actual.getProps().setProperty("key", "value");
        actual.setDagSkipWhenFail(true);
        actual.setDagRunAlone(false);
        actual.setDagName("fooDag");
        actual.setDagDependencies("jobA,jobB");
        actual.setRetryInterval(500);
        actual.setRetryTimes(3);
        assertThat(YamlEngine.marshal(actual), is(DAG_YAML));
    }

    @Test
    public void assertUnMarshalWithDag() {
        JobConfigurationPOJO actual = YamlEngine.unmarshal(DAG_YAML, JobConfigurationPOJO.class);
        assertThat(actual.getDagName(), is("fooDag"));
        assertThat(actual.getDagDependencies(), is("jobA,jobB"));
        assertThat(actual.getRetryInterval(), is(500));
        assertThat(actual.getRetryTimes(), is(3));
        assertTrue(actual.isDagSkipWhenFail());
        assertFalse(actual.isDagRunAlone());
    }
}
