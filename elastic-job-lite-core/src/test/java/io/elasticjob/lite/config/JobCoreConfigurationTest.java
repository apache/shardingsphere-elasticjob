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

package io.elasticjob.lite.config;

import io.elasticjob.lite.executor.handler.JobProperties;
import io.elasticjob.lite.executor.handler.impl.DefaultJobExceptionHandler;
import io.elasticjob.lite.fixture.handler.IgnoreJobExceptionHandler;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobCoreConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        JobCoreConfiguration actual = JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3)
                .shardingItemParameters("0=a,1=b,2=c").jobParameter("param").failover(true).misfire(false).description("desc")
                .jobProperties("job_exception_handler", IgnoreJobExceptionHandler.class.getName()).build();
        assertRequiredProperties(actual);
        assertThat(actual.getShardingItemParameters(), is("0=a,1=b,2=c"));
        assertThat(actual.getJobParameter(), is("param"));
        assertTrue(actual.isFailover());
        assertFalse(actual.isMisfire());
        assertThat(actual.getDescription(), is("desc"));
        assertThat(actual.getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(IgnoreJobExceptionHandler.class.getName()));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        JobCoreConfiguration actual = JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build();
        assertRequiredProperties(actual);
        assertDefaultValues(actual);
    }
    
    @Test
    public void assertBuildWhenOptionalParametersIsNull() {
        //noinspection NullArgumentToVariableArgMethod
        JobCoreConfiguration actual = JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).shardingItemParameters(null).jobParameter(null).description(null).build();
        assertRequiredProperties(actual);
        assertDefaultValues(actual);
    }
    
    private void assertRequiredProperties(final JobCoreConfiguration actual) {
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    private void assertDefaultValues(final JobCoreConfiguration actual) {
        assertThat(actual.getShardingItemParameters(), is(""));
        assertThat(actual.getJobParameter(), is(""));
        assertFalse(actual.isFailover());
        assertTrue(actual.isMisfire());
        assertThat(actual.getDescription(), is(""));
        assertThat(actual.getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), is(DefaultJobExceptionHandler.class.getName()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWhenJobNameIsNull() {
        JobCoreConfiguration.newBuilder(null, "0/1 * * * * ?", 3).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWhenCronIsNull() {
        JobCoreConfiguration.newBuilder("test_job", null, 3).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWhenTotalSHardingCountIsNegative() {
        JobCoreConfiguration.newBuilder(null, "0/1 * * * * ?", -1).build();
    }
}
