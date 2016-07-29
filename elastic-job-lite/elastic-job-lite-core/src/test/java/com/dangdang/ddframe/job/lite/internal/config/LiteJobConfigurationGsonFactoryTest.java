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

import com.dangdang.ddframe.job.api.config.JobCoreConfiguration;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LiteJobConfigurationGsonFactoryTest {
    
    private String simpleJobJson =  "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003da,1\\u003db\",\"jobParameter\":\"param\",\"failover\":true,\"misfire\":false,\"description\":\"desc\","
            + "\"jobProperties\":{\"executor_service_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler\","
            + "\"job_exception_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler\"},"
            + "\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\",\"disabled\":true,\"overwrite\":true}";
    
    private String dataflowJobJson = "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestDataflowJob\",\"jobType\":\"DATAFLOW\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":{\"executor_service_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler\","
            + "\"job_exception_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler\"},"
            + "\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyClass\":\"\",\"disabled\":false,\"overwrite\":false,"
            + "\"dataflowType\":\"SEQUENCE\",\"streamingProcess\":true,\"concurrentDataProcessThreadCount\":10}";
    
    private String scriptJobJson = "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":{\"executor_service_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultExecutorServiceHandler\","
            + "\"job_exception_handler\":\"com.dangdang.ddframe.job.api.internal.executor.DefaultJobExceptionHandler\"},"
            + "\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyClass\":\"\",\"disabled\":false,\"overwrite\":false,\"scriptCommandLine\":\"test.sh\"}";
    
    private String propertiesClassNotFoundJson =  "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\","
            + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"jobParameter\":\"\",\"failover\":false,\"misfire\":true,\"description\":\"\","
            + "\"jobProperties\":{\"executor_service_handler\":\"not_existed_handler\",\"job_exception_handler\":\"not_existed_handler\"},"
            + "\"monitorExecution\":false,\"maxTimeDiffSeconds\":1000,\"monitorPort\":8888,\"jobShardingStrategyClass\":\"testClass\",\"disabled\":true,\"overwrite\":true}";
    
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
        assertThat(actual.getTypeConfig().getJobClass().getCanonicalName(), is(TestSimpleJob.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getJobType(), is(JobType.SIMPLE));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is("0=a,1=b"));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is("param"));
        assertTrue(actual.getTypeConfig().getCoreConfig().isFailover());
        assertFalse(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is("desc"));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
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
        assertThat(actual.getTypeConfig().getJobClass().getCanonicalName(), is(TestDataflowJob.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getJobType(), is(JobType.DATAFLOW));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getTypeConfig().getCoreConfig().isFailover());
        assertTrue(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertThat(((DataflowJobConfiguration) actual.getTypeConfig()).getDataflowType(), is(DataflowJobConfiguration.DataflowType.SEQUENCE));
        assertTrue(((DataflowJobConfiguration) actual.getTypeConfig()).isStreamingProcess());
        assertThat(((DataflowJobConfiguration) actual.getTypeConfig()).getConcurrentDataProcessThreadCount(), is(10));
    }
    
    @Test
    public void assertFromJsonForScriptJob() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.getGson().fromJson(scriptJobJson, LiteJobConfiguration.class);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getJobClass().getCanonicalName(), is(ScriptJob.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getJobType(), is(JobType.SCRIPT));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getTypeConfig().getCoreConfig().isFailover());
        assertTrue(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
        assertThat(((ScriptJobConfiguration) actual.getTypeConfig()).getScriptCommandLine(), is("test.sh"));
    }
    
    @Test
    public void assertFromJsonForJobClassIsNotFound() {
        assertNull(LiteJobConfigurationGsonFactory.getGson().fromJson("{\"jobClass\":\"NotExistedJob\"}", LiteJobConfiguration.class));
    }
    
    @Test
    public void assertFromJsonForJobPropertiesClassesAreNotFound() {
        LiteJobConfiguration actual = LiteJobConfigurationGsonFactory.getGson().fromJson(propertiesClassNotFoundJson, LiteJobConfiguration.class);
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER).getCanonicalName(),
                is(DefaultJobExceptionHandler.class.getCanonicalName()));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobProperties().get(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER).getCanonicalName(),
                is(DefaultExecutorServiceHandler.class.getCanonicalName()));
    }
}
