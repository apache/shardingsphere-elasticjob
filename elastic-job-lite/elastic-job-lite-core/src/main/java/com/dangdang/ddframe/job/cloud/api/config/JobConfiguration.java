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

package com.dangdang.ddframe.job.cloud.api.config;

import com.dangdang.ddframe.job.cloud.api.ElasticJob;
import com.dangdang.ddframe.job.cloud.api.config.impl.JobType;

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
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    int getShardingTotalCount();
    
    /**
     * 获取作业启动时间的cron表达式.
     * 
     * @return 作业启动时间的cron表达式
     */
    String getCron();
    
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
     * 获取监控作业执行时状态.
     * 
     * <p>
     * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.
     * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
     * </p>
     * 
     * @return 监控作业执行时状态
     */
    boolean isMonitorExecution();
    
     /**
     * 获取最大容忍的本机与注册中心的时间误差秒数.
     * 
     * <p>
     * 如果时间误差超过配置秒数则作业启动时将抛异常.
     * 配置为-1表示不检查时间误差.
     * </p>
     * 
     * @return 最大容忍的本机与注册中心的时间误差秒数
     */
    int getMaxTimeDiffSeconds();
    
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
     * 获取作业辅助监控端口.
     * 
     * @return 作业辅助监控端口
     */
    int getMonitorPort();
    
    /**
     * 获取作业分片策略实现类全路径.
     * 
     * <p>
     * 默认使用{@code com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy}.
     * </p>
     * 
     * @return 作业分片策略实现类全路径
     */
    String getJobShardingStrategyClass();
    
    /**
     * 获取作业描述信息.
     * 
     * @return 作业描述信息
     */
    String getDescription();
    
    /**
     * 获取作业是否禁止启动.
     * 
     * <p>
     * 可用于部署作业时, 先禁止启动, 部署结束后统一启动.
     * </p>
     * 
     * @return 作业是否禁止启动
     */
    boolean isDisabled();
    
    /**
     * 获取本地配置是否可覆盖注册中心配置.
     * 
     * <p>
     * 如果可覆盖, 每次启动作业都以本地配置为准.
     * </p>
     * 
     * @return 本地配置是否可覆盖注册中心配置
     */
    boolean isOverwrite();
}
