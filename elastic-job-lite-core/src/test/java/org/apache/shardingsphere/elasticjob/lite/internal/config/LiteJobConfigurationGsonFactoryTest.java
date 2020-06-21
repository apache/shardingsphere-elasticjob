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

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties.JobPropertiesEnum;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.impl.DefaultExecutorServiceHandler;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LiteJobConfigurationGsonFactoryTest {
    
    private String simpleJobJson = "{\"jobName\":\"test_job\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":false,"
            + "\"description\":\"\","
            + "\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,"
            + "\"jobShardingStrategyType\":\"AVG_ALLOCATION\",\"reconcileIntervalMinutes\":15,\"disabled\":true,\"overwrite\":true}";
    
    private String dataflowJobJson = "{\"jobName\":\"test_job\","
            + "\"jobType\":\"DATAFLOW\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,"
            + "\"description\":\"\","
            + "\"streamingProcess\":true,"
            + "\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyType\":\"\",\"reconcileIntervalMinutes\":10,\"disabled\":false,\"overwrite\":false}";
    
    private String scriptJobJson = "{\"jobName\":\"test_job\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,"
            + "\"description\":\"\","
            + "\"scriptCommandLine\":\"test.sh\",\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,"
            + "\"jobShardingStrategyType\":\"\",\"reconcileIntervalMinutes\":10,\"disabled\":false,\"overwrite\":false}";
    
    @Test
    public void assertToJsonForSimpleJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).misfire(false).build()))
                .monitorExecution(false).maxTimeDiffSeconds(1000).monitorPort(8888).jobShardingStrategyType("AVG_ALLOCATION").disabled(true).overwrite(true).reconcileIntervalMinutes(15).build();
        assertThat(LiteJobConfigurationGsonFactory.toJson(actual), is(simpleJobJson));
    }
    
    @Test
    public void assertToJsonForDataflowJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), true)).build();
        assertThat(LiteJobConfigurationGsonFactory.toJson(actual), is(dataflowJobJson));
    }
    
    @Test
    public void assertToJsonForScriptJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new ScriptJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), "test.sh")).build();
        assertThat(LiteJobConfigurationGsonFactory.toJson(actual), is(scriptJobJson)); 
    }
    
    @Test
    public void assertFromJsonForSimpleJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.fromJson(simpleJobJson);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getJobType(), CoreMatchers.is(JobType.SIMPLE));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertTrue(actual.getTypeConfig().getCoreConfig().isFailover());
        assertFalse(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER), is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertFalse(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(1000));
        assertThat(actual.getMonitorPort(), is(8888));
        assertThat(actual.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getReconcileIntervalMinutes(), is(15));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertFromJsonForDataflowJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.fromJson(dataflowJobJson);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getJobType(), is(JobType.DATAFLOW));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getTypeConfig().getCoreConfig().isFailover());
        assertTrue(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyType(), is(""));
        assertThat(actual.getReconcileIntervalMinutes(), is(10));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertTrue(((DataflowJobConfiguration) actual.getTypeConfig()).isStreamingProcess());
    }
    
    @Test
    public void assertFromJsonForScriptJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.fromJson(scriptJobJson);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getJobType(), is(JobType.SCRIPT));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getTypeConfig().getCoreConfig().isFailover());
        assertTrue(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER), is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyType(), is(""));
        assertThat(actual.getReconcileIntervalMinutes(), is(10));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertThat(((ScriptJobConfiguration) actual.getTypeConfig()).getScriptCommandLine(), is("test.sh"));
    }
}
