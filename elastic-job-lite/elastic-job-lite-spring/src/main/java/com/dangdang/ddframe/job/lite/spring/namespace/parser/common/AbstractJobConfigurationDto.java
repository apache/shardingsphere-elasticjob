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

package com.dangdang.ddframe.job.lite.spring.namespace.parser.common;

import com.dangdang.ddframe.job.lite.api.ElasticJob;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.JobType;
import com.dangdang.ddframe.job.lite.api.config.impl.AbstractJobConfiguration.AbstractJobConfigurationBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 基本作业配置命名空间对象.
 *
 * @author caohao
 */
@Getter
@Setter
@RequiredArgsConstructor
public abstract class AbstractJobConfigurationDto<T extends JobConfiguration, J extends ElasticJob, B extends AbstractJobConfigurationBuilder> {
    
    private final String jobName;
    
    private final JobType jobType;
    
    private final Class<? extends J> jobClass;
    
    private final Integer shardingTotalCount;
    
    private final String cron;
    
    private String shardingItemParameters;
    
    private String jobParameter;
    
    private Boolean monitorExecution;
    
    private Integer maxTimeDiffSeconds;
    
    private Boolean failover;
    
    private Boolean misfire;
    
    private Integer monitorPort;
    
    private String jobShardingStrategyClass;
    
    private String description;
    
    private Boolean disabled;
    
    private Boolean overwrite;
    
    protected B createBuilder() {
        B builder = createCustomizedBuilder();
        buildBaselProperties(builder);
        return buildCustomizedProperties(builder);
    }
    
    private void buildBaselProperties(final B builder) {
        if (null != getShardingItemParameters()) {
            builder.shardingItemParameters(getShardingItemParameters());
        }
        if (null != getJobParameter()) {
            builder.jobParameter(getJobParameter());    
        }
        if (null != this.getMonitorExecution()) {
            builder.monitorExecution(getMonitorExecution());
        }
        if (null != getMaxTimeDiffSeconds()) {
            builder.maxTimeDiffSeconds(getMaxTimeDiffSeconds());
        }
        if (null != getMonitorPort()) {
            builder.monitorPort(getMonitorPort());
        }
        if (null != getJobShardingStrategyClass()) {
            builder.jobShardingStrategyClass(getJobShardingStrategyClass());
        }
        if (null != getDescription()) {
            builder.description(getDescription());
        }
        validateAndBuildJobStatus(builder);
    }
    
    private void validateAndBuildJobStatus(final B builder) {
        if (null != getFailover()) {
            builder.failover(getFailover());
        }
        if (null != getMisfire()) {
            builder.misfire(getMisfire());
        }
        if (null != getDisabled()) {
            builder.disabled(getDisabled());
        }
        if (null != getOverwrite()) {
            builder.overwrite(getOverwrite());
        }
    }
    
    protected abstract B createCustomizedBuilder();
    
    protected abstract B buildCustomizedProperties(final B builder);
    
    public abstract T toJobConfiguration();
}
