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

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.internal.job.JobType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 基本作业配置信息.
 * 
 * @author zhangliang
 * @author caohao
 */
@Getter
@Setter
@RequiredArgsConstructor
public class BaseJobConfiguration<T extends ElasticJob> implements JobConfiguration<T> {
    
    private final String jobName;
    
    private final JobType jobType = JobType.SIMPLE;
    
    private final Class<T> jobClass;
    
    private final int shardingTotalCount;
    
    private final String cron;
    
    private String shardingItemParameters = "";
    
    private String jobParameter = "";
    
    private boolean monitorExecution = true;
    
    private int maxTimeDiffSeconds = -1;
    
    private boolean failover;
    
    private boolean misfire = true;
    
    private int monitorPort = -1;
    
    private String jobShardingStrategyClass = "";
    
    private String description = "";
    
    private boolean disabled;
    
    private boolean overwrite;
}
