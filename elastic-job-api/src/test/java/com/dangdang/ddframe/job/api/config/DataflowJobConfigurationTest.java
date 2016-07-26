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
import com.dangdang.ddframe.job.api.config.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfiguration;
import com.dangdang.ddframe.job.api.type.dataflow.DataflowJobConfiguration;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.unitils.util.ReflectionUtils;

import static org.junit.Assert.assertThat;

public final class DataflowJobConfigurationTest extends AbstractJobConfigurationTest<DataflowJobConfiguration, DataflowJobConfiguration.DataflowJobConfigurationBuilder> {
    
    private int concurrentDataProcessThreadCount = 100;
    
    private int processCountIntervalSeconds = 10;
    
    private boolean streamingProcess = true;
    
    @Override
    protected DataflowJobConfiguration getJobConfiguration() {
        return getJobConfigurationBuilder().build();
    }
    
    @Override
    protected DataflowJobConfiguration.DataflowJobConfigurationBuilder getJobConfigurationBuilder() {
        return new DataflowJobConfiguration.DataflowJobConfigurationBuilder("dataflowJob", TestDataflowJob.class, "0/1 * * * * ?", 10, DataflowJobConfiguration.DataflowType.THROUGHPUT);
    }
    
    @Override
    @Test
    public void assertBuildJobConfigurationWithCustomizedProperties() {
        JobConfiguration jobConfiguration = getJobConfiguration();
        try {
            ReflectionUtils.setFieldValue(jobConfiguration, DataflowJobConfiguration.class.getDeclaredField("concurrentDataProcessThreadCount"), concurrentDataProcessThreadCount);
            ReflectionUtils.setFieldValue(jobConfiguration, DataflowJobConfiguration.class.getDeclaredField("streamingProcess"), streamingProcess);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        assertThat(getJobConfigurationBuilder().concurrentDataProcessThreadCount(concurrentDataProcessThreadCount)
                        .streamingProcess(streamingProcess)
                        .build(),
                new ReflectionEquals(jobConfiguration));
    }
    
    @Test
    public void assertBuildDataflowJobConfigurationWithDefaultProperties() {
        JobConfiguration jobConfiguration = getJobConfiguration();
        assertThat(getJobConfigurationBuilder().build(),
                new ReflectionEquals(jobConfiguration));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBuildDataflowJobConfigurationWithWrongConcurrentDataProcessThreadCount() {
        AbstractJobConfiguration.AbstractJobConfigurationBuilder builder = getJobConfigurationBuilder();
        try {
            ReflectionUtils.setFieldValue(builder, DataflowJobConfiguration.DataflowJobConfigurationBuilder.class.getDeclaredField("concurrentDataProcessThreadCount"), 0);
        } catch (final NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        builder.build();
    }
}
