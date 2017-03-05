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

package com.dangdang.ddframe.job.config;

import com.dangdang.ddframe.job.api.JobType;

/**
 * 作业类型配置.
 * 
 * @author caohao
 * @author zhangliang
 */
public interface JobTypeConfiguration {
    
    /**
     * 获取作业类型.
     * 
     * @return 作业类型
     */
    JobType getJobType();
    
    /**
     * 获取作业实现类名称.
     *
     * @return 作业实现类名称
     */
    String getJobClass();
    
    /**
     * 获取作业核心配置.
     * 
     * @return 作业核心配置
     */
    JobCoreConfiguration getCoreConfig(); 
}
