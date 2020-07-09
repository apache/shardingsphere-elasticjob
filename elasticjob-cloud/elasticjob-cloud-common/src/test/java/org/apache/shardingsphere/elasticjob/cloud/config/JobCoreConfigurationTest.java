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

package org.apache.shardingsphere.elasticjob.cloud.config;

import org.apache.shardingsphere.elasticjob.cloud.executor.handler.impl.DefaultJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.fixture.handler.IgnoreJobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.JobProperties;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public final class JobCoreConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        JobCoreConfiguration actual = JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3)
                .shardingItemParameters("0=a,1=b,2=c").jobParameter("param").failover(true).misfire(false).description("desc")
                .jobProperties("job_exception_handler", IgnoreJobExceptionHandler.class.getName()).build();
        assertRequiredProperties(actual);
        Assert.assertThat(actual.getShardingItemParameters(), Is.is("0=a,1=b,2=c"));
        Assert.assertThat(actual.getJobParameter(), Is.is("param"));
        Assert.assertTrue(actual.isFailover());
        Assert.assertFalse(actual.isMisfire());
        Assert.assertThat(actual.getDescription(), Is.is("desc"));
        Assert.assertThat(actual.getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), Is.is(IgnoreJobExceptionHandler.class.getName()));
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
        Assert.assertThat(actual.getJobName(), Is.is("test_job"));
        Assert.assertThat(actual.getCron(), Is.is("0/1 * * * * ?"));
        Assert.assertThat(actual.getShardingTotalCount(), Is.is(3));
    }
    
    private void assertDefaultValues(final JobCoreConfiguration actual) {
        Assert.assertThat(actual.getShardingItemParameters(), Is.is(""));
        Assert.assertThat(actual.getJobParameter(), Is.is(""));
        Assert.assertFalse(actual.isFailover());
        Assert.assertTrue(actual.isMisfire());
        Assert.assertThat(actual.getDescription(), Is.is(""));
        Assert.assertThat(actual.getJobProperties().get(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER), Is.is(DefaultJobExceptionHandler.class.getName()));
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
