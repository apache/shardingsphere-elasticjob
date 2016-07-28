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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.api.JobCoreConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobProperties;
import com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler;
import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJob;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LiteJobConfigurationGsonFactoryTest {
    
    private String simpleJobJson =  "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003da,1\\u003db\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"desc\","
            + "\"jobProperties\":{\"executor_service_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler\","
            + "\"job_exception_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler\"},"
            + "\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\",\"disabled\":true,\"overwrite\":true}";
    
    private String dataflowJobJson =  "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestDataflowJob\",\"jobType\":\"DATAFLOW\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":{},\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyClass\":\"\",\"disabled\":false,\"overwrite\":false,"
            + "\"dataflowType\":\"SEQUENCE\",\"streamingProcess\":true,\"concurrentDataProcessThreadCount\":10}";
    
    private String scriptJobJson = "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\",\"jobProperties\":{},"
            + "\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyClass\":\"\",\"disabled\":false,\"overwrite\":false,\"scriptCommandLine\":\"test.sh\"}";
    
    @Test
    public void assertToJsonForSimpleJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3)
                .shardingItemParameters("0=a,1=b").jobParameter("param").failover(true).misfire(false).description("desc")
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), DefaultJobExceptionHandler.class)
                .jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), DefaultExecutorServiceHandler.class)
                .build(), TestSimpleJob.class))
                .monitorExecution(false).maxTimeDiffSeconds(1000).monitorPort(8888).jobShardingStrategyClass("testClass").disabled(true).overwrite(true).build();
        assertThat(LiteJobConfigurationGsonFactory.getGson().toJson(actual), is(simpleJobJson));
    }
    
    @Test
    public void assertToJsonForDataflowJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), 
                TestDataflowJob.class, DataflowJobConfiguration.DataflowType.SEQUENCE, true, 10)).build();
        assertThat(LiteJobConfigurationGsonFactory.getGson().toJson(actual), is(dataflowJobJson));
    }
    
    @Test
    public void assertToJsonForScriptJob() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new ScriptJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), "test.sh")).build();
        assertThat(LiteJobConfigurationGsonFactory.getGson().toJson(actual), is(scriptJobJson)); 
    }
    
    @Test
    public void assertFromJsonForSimpleJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.getGson().fromJson(simpleJobJson, LiteJobConfiguration.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getJobConfig().getJobClass().getCanonicalName(), is(TestSimpleJob.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getJobType(), is(JobType.SIMPLE));
        assertThat(actual.getJobConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingItemParameters(), is("0=a,1=b"));
        assertThat(actual.getJobConfig().getCoreConfig().getJobParameter(), is("param"));
        assertTrue(actual.getJobConfig().getCoreConfig().isFailover());
        assertFalse(actual.getJobConfig().getCoreConfig().isMisfire());
        assertThat(actual.getJobConfig().getCoreConfig().getDescription(), is("desc"));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertFalse(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(1000));
        assertThat(actual.getMonitorPort(), is(8888));
        assertThat(actual.getJobShardingStrategyClass(), is("testClass"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertFromJsonForDataflowJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.getGson().fromJson(dataflowJobJson, LiteJobConfiguration.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getJobConfig().getJobClass().getCanonicalName(), is(TestDataflowJob.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getJobType(), is(JobType.DATAFLOW));
        assertThat(actual.getJobConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getJobConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getJobConfig().getCoreConfig().isFailover());
        assertTrue(actual.getJobConfig().getCoreConfig().isMisfire());
        assertThat(actual.getJobConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertThat(((DataflowJobConfiguration) actual.getJobConfig()).getDataflowType(), is(DataflowJobConfiguration.DataflowType.SEQUENCE));
        assertTrue(((DataflowJobConfiguration) actual.getJobConfig()).isStreamingProcess());
        assertThat(((DataflowJobConfiguration) actual.getJobConfig()).getConcurrentDataProcessThreadCount(), is(10));
    }
    
    @Test
    public void assertFromJsonForScriptJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.getGson().fromJson(scriptJobJson, LiteJobConfiguration.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getJobConfig().getJobClass().getCanonicalName(), is(ScriptJob.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getJobType(), is(JobType.SCRIPT));
        assertThat(actual.getJobConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getJobConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getJobConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getJobConfig().getCoreConfig().isFailover());
        assertTrue(actual.getJobConfig().getCoreConfig().isMisfire());
        assertThat(actual.getJobConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getJobConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertThat(((ScriptJobConfiguration) actual.getJobConfig()).getScriptCommandLine(), is("test.sh"));
    }
}
