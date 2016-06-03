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

package com.dangdang.ddframe.job.api.config;

import com.dangdang.ddframe.job.api.config.impl.AbstractJobConfiguration.AbstractJobConfigurationBuilder;
import com.dangdang.ddframe.job.api.config.impl.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.api.config.impl.DataFlowJobConfiguration.DataFlowJobConfigurationBuilder;
import com.dangdang.ddframe.job.fixture.TestDataFlowJob;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import static org.junit.Assert.assertThat;

public final class DataFlowJobConfigurationTest extends AbstractJobConfigurationTest<DataFlowJobConfiguration, DataFlowJobConfigurationBuilder> {
    
    private int concurrentDataProcessThreadCount = 100;
    
    private int fetchDataCount = 20;
    
    private int processCountIntervalSeconds = 10;
    
    @Override
    protected DataFlowJobConfiguration getJobConfiguration() {
        return getJobConfigurationBuilder().build();
    }
    
    @Override
    protected DataFlowJobConfigurationBuilder getJobConfigurationBuilder() {
        return new DataFlowJobConfigurationBuilder("dataFlowJob", TestDataFlowJob.class, 10, "0/1 * * * * ?");
    }
    
    @Override
    @Test
    public void testBuildJobConfigurationWithCustomizedProperties() {
        JobConfiguration jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, DataFlowJobConfiguration.class.getDeclaredField("concurrentDataProcessThreadCount"), concurrentDataProcessThreadCount);
            ReflectionUtils.setFieldValue(jobConfiguration, DataFlowJobConfiguration.class.getDeclaredField("fetchDataCount"), fetchDataCount);
            ReflectionUtils.setFieldValue(jobConfiguration, DataFlowJobConfiguration.class.getDeclaredField("processCountIntervalSeconds"), processCountIntervalSeconds);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        assertThat(getJobConfigurationBuilder().concurrentDataProcessThreadCount(concurrentDataProcessThreadCount)
                        .fetchDataCount(fetchDataCount)
                        .processCountIntervalSeconds(processCountIntervalSeconds)
                        .build(),
                new ReflectionEquals(jobConfiguration));
    }
    
    @Test
    public void testBuildDataFlowJobConfigurationWithDefaultProperties() {
        JobConfiguration jobConfiguration = getJobConfiguration();
        assertThat(getJobConfigurationBuilder().build(),
                new ReflectionEquals(jobConfiguration));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDataFlowJobConfigurationWithWrongConcurrentDataProcessThreadCount() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, DataFlowJobConfigurationBuilder.class.getDeclaredField("concurrentDataProcessThreadCount"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDataFlowJobConfigurationWithWrongFetchDataCount() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, DataFlowJobConfigurationBuilder.class.getDeclaredField("fetchDataCount"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBuildDataFlowJobConfigurationWithWrongProcessCountIntervalSeconds() {
        AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, DataFlowJobConfigurationBuilder.class.getDeclaredField("processCountIntervalSeconds"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
}
