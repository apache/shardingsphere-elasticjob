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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.dataflow;

import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.dangdang.ddframe.job.lite.api.config.impl.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.ThroughputDataflowElasticJob;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationBuilder;
import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationDto;
import static org.junit.Assert.assertThat;

public final class DataflowJobConfigurationDtoTest {
    
    @Test
    public void testBuildDataflowJobConfigurationDtoWithCustomizedProperties() {
        int concurrentDataProcessThreadCount = 200;
        int processCountIntervalSeconds = 1000;
        DataflowJobConfigurationDto jobConfigurationDto = createDataflowJobConfigurationDto();
        jobConfigurationDto.setConcurrentDataProcessThreadCount(concurrentDataProcessThreadCount);
        jobConfigurationDto.setProcessCountIntervalSeconds(processCountIntervalSeconds);
        jobConfigurationDto.setStreamingProcess(true);
        DataflowJobConfiguration.DataflowJobConfigurationBuilder builder = 
                (DataflowJobConfiguration.DataflowJobConfigurationBuilder) buildJobConfigurationBuilder(createDataflowJobConfigurationBuilder());
        assertThat(buildJobConfigurationDto(jobConfigurationDto), new ReflectionEquals(builder.concurrentDataProcessThreadCount(concurrentDataProcessThreadCount)
                .processCountIntervalSeconds(processCountIntervalSeconds).streamingProcess(true).build()));
    }
    
    @Test
    public void testBuildDataflowJobConfigurationDtoWithDefaultProperties() {
        assertThat(createDataflowJobConfigurationDto().toJobConfiguration(), new ReflectionEquals(createDataflowJobConfigurationBuilder().build()));
    }
    
    private DataflowJobConfigurationDto createDataflowJobConfigurationDto() {
        return new DataflowJobConfigurationDto("dataflowJob", ThroughputDataflowElasticJob.class, 10, "0/1 * * * * ?", DataflowType.THROUGHPUT);
    }
    
    private DataflowJobConfiguration.DataflowJobConfigurationBuilder createDataflowJobConfigurationBuilder() {
        return new DataflowJobConfiguration.DataflowJobConfigurationBuilder("dataflowJob", ThroughputDataflowElasticJob.class, 10, "0/1 * * * * ?", DataflowType.THROUGHPUT);
    }
}
