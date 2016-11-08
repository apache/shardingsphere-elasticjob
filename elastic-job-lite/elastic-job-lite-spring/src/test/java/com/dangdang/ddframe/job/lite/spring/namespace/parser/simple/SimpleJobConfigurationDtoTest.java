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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.simple;

import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.FooSimpleElasticJob;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SimpleJobConfigurationDtoTest {
    
    @Test
    public void assertToLiteJobConfigurationForAllProperties() {
        SimpleJobConfigurationDto jobConfigDto = new SimpleJobConfigurationDto("simpleJob", "0/1 * * * * ?", 10, FooSimpleElasticJob.class);
        jobConfigDto.setShardingItemParameters("0=a,1=b");
        jobConfigDto.setJobParameter("param");
        jobConfigDto.setFailover(true);
        jobConfigDto.setMisfire(false);
        jobConfigDto.setDescription("desc");
        jobConfigDto.setMonitorExecution(true);
        jobConfigDto.setMaxTimeDiffSeconds(10000);
        jobConfigDto.setMonitorPort(8888);
        jobConfigDto.setJobShardingStrategyClass("test_class");
        jobConfigDto.setDisabled(true);
        jobConfigDto.setOverwrite(true);
        LiteJobConfiguration actual = jobConfigDto.toLiteJobConfiguration();
        assertThat(actual.getJobName(), is("simpleJob"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(10));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is("0=a,1=b"));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is("param"));
        assertTrue(actual.getTypeConfig().getCoreConfig().isFailover());
        assertFalse(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is("desc"));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(10000));
        assertThat(actual.getMonitorPort(), is(8888));
        assertThat(actual.getJobShardingStrategyClass(), is("test_class"));
        assertTrue(actual.isDisabled());
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertToLiteJobConfigurationWhenOptionalParametersIsNull() {
        LiteJobConfiguration actual = new SimpleJobConfigurationDto("simpleJob", "0/1 * * * * ?", 10, FooSimpleElasticJob.class).toLiteJobConfiguration();
        assertThat(actual.getJobName(), is("simpleJob"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(10));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingItemParameters(), is(""));
        assertThat(actual.getTypeConfig().getCoreConfig().getJobParameter(), is(""));
        assertFalse(actual.getTypeConfig().getCoreConfig().isFailover());
        assertTrue(actual.getTypeConfig().getCoreConfig().isMisfire());
        assertThat(actual.getTypeConfig().getCoreConfig().getDescription(), is(""));
        assertTrue(actual.isMonitorExecution());
        assertThat(actual.getMaxTimeDiffSeconds(), is(-1));
        assertThat(actual.getMonitorPort(), is(-1));
        assertThat(actual.getJobShardingStrategyClass(), is(""));
        assertFalse(actual.isDisabled());
        assertFalse(actual.isOverwrite());
    }
}
