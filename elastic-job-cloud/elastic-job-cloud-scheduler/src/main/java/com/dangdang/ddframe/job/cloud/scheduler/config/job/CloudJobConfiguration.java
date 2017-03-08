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

package com.dangdang.ddframe.job.cloud.scheduler.config.job;

import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 云作业配置对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public final class CloudJobConfiguration implements JobRootConfiguration {
    
    private final String appName;
    
    private final JobTypeConfiguration typeConfig;
    
    private final double cpuCount;
    
    private final double memoryMB;
    
    private final CloudJobExecutionType jobExecutionType;
    
    private String beanName;
    
    private String applicationContext; 
    
    /**
     * 获取作业名称.
     *
     * @return 作业名称
     */
    public String getJobName() {
        return typeConfig.getCoreConfig().getJobName();
    }
}
