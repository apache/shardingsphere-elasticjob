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

package org.apache.shardingsphere.elasticjob.api;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?")
                .shardingItemParameters("0=a,1=b,2=c").jobParameter("param")
                .monitorExecution(false).failover(true).misfire(false)
                .maxTimeDiffSeconds(1000).reconcileIntervalMinutes(60)
                .jobShardingStrategyType("AVG_ALLOCATION").jobExecutorServiceHandlerType("SINGLE_THREAD").jobErrorHandlerType("IGNORE")
                .description("desc").setProperty("key", "value")
                .disabled(true).overwrite(true).build();
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItemParameters(), is("0=a,1=b,2=c"));
        assertThat(actual.getJobParameter(), is("param"));
        assertFalse(actual.isMonitorExecution());
        assertTrue(actual.isFailover());
        assertFalse(actual.isMisfire());
        assertThat(actual.getMaxTimeDiffSeconds(), is(1000));
        assertThat(actual.getReconcileIntervalMinutes(), is(60));
        assertThat(actual.getJobShardingStrategyType(), is("AVG_ALLOCATION"));
        assertThat(actual.getJobExecutorServiceHandlerType(), is("SINGLE_THREAD"));
        assertThat(actual.getJobErrorHandlerType(), is("IGNORE"));
        assertThat(actual.getDescription(), is("desc"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        JobConfiguration actual = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItemParameters(), is(""));
        assertThat(actual.getJobParameter(), is(""));
        assertTrue(actual.isMonitorExecution());
        assertFalse(actual.isFailover());
        assertTrue(actual.isMisfire());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getReconcileIntervalMinutes(), is(10));
        assertNull(actual.getJobShardingStrategyType());
        assertNull(actual.getJobExecutorServiceHandlerType());
        assertNull(actual.getJobErrorHandlerType());
        assertThat(actual.getDescription(), is(""));
        assertTrue(actual.getProps().isEmpty());
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithEmptyJobName() {
        JobConfiguration.newBuilder("", 3).cron("0/1 * * * * ?").build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildWithInvalidShardingTotalCount() {
        JobConfiguration.newBuilder("test_job", -1).cron("0/1 * * * * ?").build();
    }
}
