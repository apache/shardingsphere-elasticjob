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

package org.apache.shardingsphere.elasticjob.lite.config;

import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LiteJobConfigurationTest {
    
    @Test
    public void assertBuildAllProperties() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build()))
                .monitorExecution(false).maxTimeDiffSeconds(1000).monitorPort(8888).jobShardingStrategyClass("testClass").disabled(true).overwrite(true).reconcileIntervalMinutes(60).build();
        assertFalse(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(1000));
        assertThat(actual.getMonitorPort(), is(8888));
        assertThat(actual.getJobShardingStrategyClass(), is("testClass"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
        assertThat(actual.getReconcileIntervalMinutes(), is(60));
    }
    
    @Test
    public void assertBuildRequiredProperties() {
        LiteJobConfiguration actual = LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build())).build();
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
    }
    
    @Test
    public void assertBuildWhenOptionalParametersIsNull() {
        assertThat(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder(
                "test_job", "0/1 * * * * ?", 3).build())).jobShardingStrategyClass(null).build().getJobShardingStrategyClass(), is(""));
    }
    
    @Test
    public void assertIsNotFailover() {
        assertFalse(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(false).build())).monitorExecution(false).build().isFailover());
    }
    
    @Test
    public void assertIsFailover() {
        assertTrue(LiteJobConfiguration.newBuilder(
                new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).failover(true).build())).monitorExecution(true).build().isFailover());
    }
}
