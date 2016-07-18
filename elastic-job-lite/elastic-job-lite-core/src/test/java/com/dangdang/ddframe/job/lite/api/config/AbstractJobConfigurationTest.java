/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 *
 */

package com.dangdang.ddframe.job.lite.api.config;

import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.AbstractJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.AbstractJobConfiguration.AbstractJobConfigurationBuilder;
import com.dangdang.ddframe.job.lite.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;

import static org.junit.Assert.assertThat;

public abstract class AbstractJobConfigurationTest<T extends JobConfiguration, B extends AbstractJobConfigurationBuilder> {
    
    private String shardingItemParameters = "0=a,1=b,2=c";
    
    private Boolean monitorExecution = false;
    
    private Integer maxTimeDiffSeconds = 100;
    
    private Boolean failover = true;
    
    private Integer monitorPort = 1000;
    
    private String jobShardingStrategyClass = AverageAllocationJobShardingStrategy.class.getName();
    
    private String description = "jobDescription";
    
    private Boolean disabled = true;
    
    private Boolean overwrite = true;
    
    @Test
    public void testBuildJobConfigurationWithCustomizedProperties() {
        assertThat(getJobConfigurationBuilder()
                .shardingItemParameters(shardingItemParameters)
                .monitorExecution(monitorExecution)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .failover(failover)
                .monitorPort(monitorPort)
                .jobShardingStrategyClass(jobShardingStrategyClass)
                .description(description)
                .disabled(disabled)
                .overwrite(overwrite)
                .build(), 
                new ReflectionEquals(buildJobConfigurationWithCustomizedProperties()));
    }
    
    private T buildJobConfigurationWithCustomizedProperties() {
        T jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("shardingItemParameters"), shardingItemParameters);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("monitorExecution"), monitorExecution);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("maxTimeDiffSeconds"), maxTimeDiffSeconds);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("failover"), failover);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("monitorPort"), monitorPort);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("jobShardingStrategyClass"), jobShardingStrategyClass);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("description"), description);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("disabled"), disabled);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("overwrite"), overwrite);
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return jobConfiguration;
    }
    
    @Test
    public void testBuildJobConfigurationWithDefaultProperties() {
        assertThat(getJobConfigurationBuilder().build(), new ReflectionEquals(buildJobConfigurationWithDefaultProperties()));
    }
    
    private T buildJobConfigurationWithDefaultProperties() {
        T jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("shardingItemParameters"), "");
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("monitorExecution"), true);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("maxTimeDiffSeconds"), -1);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("failover"), false);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("monitorPort"), -1);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("jobShardingStrategyClass"), "");
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("description"), "");
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("disabled"), false);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("overwrite"), false);
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return jobConfiguration;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongJobName() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfigurationBuilder.class.getDeclaredField("jobName"), "");
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongJobClass() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfigurationBuilder.class.getDeclaredField("jobClass"), this.getClass());
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongShardingTotalCount() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfigurationBuilder.class.getDeclaredField("shardingTotalCount"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongCron() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfigurationBuilder.class.getDeclaredField("cron"), "");
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithJobDescription() {
        getJobConfigurationBuilder().description(null).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongShardingItemParameters() {
        getJobConfigurationBuilder().shardingItemParameters(null).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongJobParameter() {
        getJobConfigurationBuilder().jobParameter(null).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongJobShardingStrategyClass() {
        getJobConfigurationBuilder().jobShardingStrategyClass(null).build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildJobConfigurationWithWrongJobDescription() {
        getJobConfigurationBuilder().description(null).build();
    }
    
    protected abstract T getJobConfiguration();
    
    protected abstract B getJobConfigurationBuilder();
}
