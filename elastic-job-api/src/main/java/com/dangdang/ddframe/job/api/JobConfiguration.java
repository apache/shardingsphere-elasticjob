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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.api.internal.ElasticJob;
import com.dangdang.ddframe.job.api.internal.config.JobType;

/**
 * 作业基本配置信息.
 * 
 * @author caohao
 */
public interface JobConfiguration<T extends ElasticJob> {
    
    /**
     * 获取作业名称.
     *
     * @return 作业名称
     */
    String getJobName();
    
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
    Class<? extends T> getJobClass();
    
    /**
     * 获取作业启动时间的cron表达式.
     *
     * @return 作业启动时间的cron表达式
     */
    String getCron();
    
    /**
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    int getShardingTotalCount();
    
    /**
     * 获取分片序列号和个性化参数对照表.
     * 
     * <p>
     * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map.
     * 分片序列号从0开始, 不可大于或等于作业分片总数.
     * 如:
     * 0=a,1=b,2=c
     * </p>
     * 
     * @return 分片序列号和个性化参数对照表
     */
    String getShardingItemParameters();
    
    /**
     * 获取作业自定义参数.
     * 
     * <p>
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     * </p>
     * 
     * @return 作业自定义参数
     */
    String getJobParameter();
    
    /**
     * 获取是否开启失效转移.
     * 
     * <p>
     * 只有对monitorExecution的情况下才可以开启失效转移.
     * </p> 
     * 
     * @return 是否开启失效转移
     */
    boolean isFailover();
    
    /**
     * 获取是否开启misfire.
     * 
     * @return misfire
     */
    boolean isMisfire();
    
    /**
     * 获取作业描述信息.
     * 
     * @return 作业描述信息
     */
    String getDescription();
}
