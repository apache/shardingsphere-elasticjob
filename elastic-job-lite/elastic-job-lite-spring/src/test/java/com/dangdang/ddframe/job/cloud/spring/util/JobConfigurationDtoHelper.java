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

package com.dangdang.ddframe.job.cloud.spring.util;

import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.impl.AbstractJobConfiguration.AbstractJobConfigurationBuilder;
import com.dangdang.ddframe.job.cloud.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;
import com.dangdang.ddframe.job.cloud.spring.namespace.parser.common.AbstractJobConfigurationDto;

public class JobConfigurationDtoHelper {
    
    private static String shardingItemParameters = "0=a,1=b,2=c";
    
    private static Boolean monitorExecution = false;
    
    private static Integer maxTimeDiffSeconds = 100;
    
    private static Boolean failover = true;
    
    private static Integer monitorPort = 1000;
    
    private static String jobShardingStrategyClass = AverageAllocationJobShardingStrategy.class.getName();
    
    private static String description = "jobDescription";
    
    private static Boolean disabled = true;
    
    private static Boolean overwrite = true;
    
    public static AbstractJobConfigurationBuilder buildJobConfigurationBuilder(final AbstractJobConfigurationBuilder jobConfigurationBuilder) {
        return jobConfigurationBuilder.shardingItemParameters(shardingItemParameters)
                    .monitorExecution(monitorExecution)
                    .maxTimeDiffSeconds(maxTimeDiffSeconds)
                    .failover(failover)
                    .monitorPort(monitorPort)
                    .jobShardingStrategyClass(jobShardingStrategyClass)
                    .disabled(disabled)
                    .overwrite(overwrite)
                    .description(description);
    }
    
    public static JobConfiguration buildJobConfigurationDto(final AbstractJobConfigurationDto jobConfigurationDto) {
        jobConfigurationDto.setShardingItemParameters(shardingItemParameters);
        jobConfigurationDto.setMonitorExecution(monitorExecution);
        jobConfigurationDto.setMaxTimeDiffSeconds(maxTimeDiffSeconds);
        jobConfigurationDto.setFailover(failover);
        jobConfigurationDto.setMonitorPort(monitorPort);
        jobConfigurationDto.setJobShardingStrategyClass(jobShardingStrategyClass);
        jobConfigurationDto.setDisabled(disabled);
        jobConfigurationDto.setOverwrite(overwrite);
        jobConfigurationDto.setDescription(description);
        return jobConfigurationDto.toJobConfiguration();
    }
}
