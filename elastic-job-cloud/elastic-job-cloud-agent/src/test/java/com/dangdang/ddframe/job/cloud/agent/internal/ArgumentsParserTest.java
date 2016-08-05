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

import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.api.executor.handler.impl.DefaultExecutorServiceHandler;
import com.dangdang.ddframe.job.api.executor.handler.impl.DefaultJobExceptionHandler;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJob;
import com.dangdang.ddframe.job.cloud.agent.fixture.TestJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class ArgumentsParserTest {
    
    private String shardingContextJson = "{\"jobName\":\"test_job\",\"shardingTotalCount\":1,\"jobParameter\":\"\",\"shardingItemParameters\":{\"0\":\"\"}}";
    
    private final String configContextJson = "{\"jobType\":\"SIMPLE\",\"executorServiceHandler\":\"" + DefaultExecutorServiceHandler.class.getCanonicalName() + "\",\"jobExceptionHandler\":\""
            + DefaultJobExceptionHandler.class.getCanonicalName() + "\",\"jobName\":\"test_job\",\"jobClass\":\"%s\"}";
    
    private final String cloudParameterJson = "{\"shardingContext\":" + shardingContextJson + ",\"jobConfigContext\":" + configContextJson + "}";
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertParseWhenArgumentsIsNotEnough() throws JobExecutionEnvironmentException {
        ArgumentsParser.parse(new String[] {});
    }
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertParseWhenClassIsNotFound() throws JobExecutionEnvironmentException {
        ArgumentsParser.parse(new String[] {"{\"jobConfigContext\":{\"jobType\":\"SIMPLE\",\"jobName\":\"test_job\",\"jobClass\":\"testClass\"}}"});
    }
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertParseWhenClassIsNotElasticJob() throws JobExecutionEnvironmentException {
        ArgumentsParser.parse(new String[]{String.format(cloudParameterJson, "notElasticJobClass")});
    }
    
    @Test
    public void assertParse() throws JobExecutionEnvironmentException {
        ArgumentsParser actual = ArgumentsParser.parse(new String[] {String.format(cloudParameterJson, TestJob.class.getCanonicalName())});
        assertThat(actual.getElasticJob(), instanceOf(TestJob.class));
        assertNotNull(actual.getShardingContext());
        assertNotNull(actual.getJobConfig());
    }
    
    @Test
    public void assertParseScriptJob() throws JobExecutionEnvironmentException {
        ArgumentsParser actual = ArgumentsParser.parse(new String[] {String.format(cloudParameterJson, ScriptJob.class.getCanonicalName())});
        assertNull(actual.getElasticJob());
        assertNotNull(actual.getShardingContext());
        assertNotNull(actual.getJobConfig());
    }
}
