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

package com.dangdang.ddframe.job.api.config;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfiguration;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractJobConfigurationTest<T extends JobConfiguration, B extends AbstractJobConfiguration.Builder> {
    
    private String shardingItemParameters = "0=a,1=b,2=c";
    
    private Boolean failover = true;
    
    private String description = "jobDescription";
    
    @Test
    public void assertBuildJobConfigurationWithCustomizedProperties() {
        assertThat(getJobConfigurationBuilder()
                .shardingItemParameters(shardingItemParameters)
                .failover(failover)
                .description(description)
                .build(), 
                new ReflectionEquals(buildJobConfigurationWithCustomizedProperties()));
    }
    
    private T buildJobConfigurationWithCustomizedProperties() {
        T jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("shardingItemParameters"), shardingItemParameters);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("failover"), failover);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("description"), description);
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return jobConfiguration;
    }
    
    @Test
    public void assertBuildJobConfigurationWithDefaultProperties() {
        assertThat(getJobConfigurationBuilder().build(), new ReflectionEquals(buildJobConfigurationWithDefaultProperties()));
    }
    
    private T buildJobConfigurationWithDefaultProperties() {
        T jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("shardingItemParameters"), "");
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("failover"), false);
            ReflectionUtils.setFieldValue(jobConfiguration, AbstractJobConfiguration.class.getDeclaredField("description"), "");
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return jobConfiguration;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobConfigurationWithWrongJobName() {
        AbstractJobConfiguration.Builder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfiguration.Builder.class.getDeclaredField("jobName"), "");
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobConfigurationWithWrongJobClass() {
        AbstractJobConfiguration.Builder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfiguration.Builder.class.getDeclaredField("jobClass"), this.getClass());
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobConfigurationWithWrongShardingTotalCount() {
        AbstractJobConfiguration.Builder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfiguration.Builder.class.getDeclaredField("shardingTotalCount"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildJobConfigurationWithWrongCron() {
        AbstractJobConfiguration.Builder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, AbstractJobConfiguration.Builder.class.getDeclaredField("cron"), "");
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test
    public void assertBuildJobConfigurationWhenShardingItemParametersIsNull() {
        assertThat(getJobConfigurationBuilder().shardingItemParameters(null).build().getShardingItemParameters(), is(""));
    }
    
    @Test
    public void assertBuildJobConfigurationWhenJobParameterIsNull() {
        assertThat(getJobConfigurationBuilder().jobParameter(null).build().getJobParameter(), is(""));
    }
    
    @Test
    public void assertBuildJobConfigurationWhenJobDescriptionIsnull() {
        assertThat(getJobConfigurationBuilder().description(null).build().getDescription(), is(""));
    }
    
    protected abstract T getJobConfiguration();
    
    protected abstract B getJobConfigurationBuilder();
}
