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

import com.dangdang.ddframe.job.lite.api.config.impl.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.ThroughputDataFlowElasticJob;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationBuilder;
import static com.dangdang.ddframe.job.lite.spring.util.JobConfigurationDtoHelper.buildJobConfigurationDto;
import static org.junit.Assert.assertThat;

public final class DataFlowJobConfigurationDtoTest {
    
    @Test
    public void testBuildDataFlowJobConfigurationDtoWithCustomizedProperties() {
        int concurrentDataProcessThreadCount = 200;
        int processCountIntervalSeconds = 1000;
        int fetchDataCount = 100;
        DataFlowJobConfigurationDto jobConfigurationDto = createDataFlowJobConfigurationDto();
        jobConfigurationDto.setConcurrentDataProcessThreadCount(concurrentDataProcessThreadCount);
        jobConfigurationDto.setProcessCountIntervalSeconds(processCountIntervalSeconds);
        jobConfigurationDto.setFetchDataCount(fetchDataCount);
        jobConfigurationDto.setStreamingProcess(true);
        DataFlowJobConfiguration.DataFlowJobConfigurationBuilder builder = 
                (DataFlowJobConfiguration.DataFlowJobConfigurationBuilder) buildJobConfigurationBuilder(createDataFlowJobConfigurationBuilder());
        assertThat(buildJobConfigurationDto(jobConfigurationDto), new ReflectionEquals(builder.concurrentDataProcessThreadCount(concurrentDataProcessThreadCount)
                .processCountIntervalSeconds(processCountIntervalSeconds).fetchDataCount(fetchDataCount).streamingProcess(true).build()));
    }
    
    @Test
    public void testBuildDataFlowJobConfigurationDtoWithDefaultProperties() {
        assertThat(createDataFlowJobConfigurationDto().toJobConfiguration(), new ReflectionEquals(createDataFlowJobConfigurationBuilder().build()));
    }
    
    private DataFlowJobConfigurationDto createDataFlowJobConfigurationDto() {
        return new DataFlowJobConfigurationDto("dataFlowJob", ThroughputDataFlowElasticJob.class, 10, "0/1 * * * * ?");
    }
    
    private DataFlowJobConfiguration.DataFlowJobConfigurationBuilder createDataFlowJobConfigurationBuilder() {
        return new DataFlowJobConfiguration.DataFlowJobConfigurationBuilder("dataFlowJob", ThroughputDataFlowElasticJob.class, 10, "0/1 * * * * ?");
    }
}
