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

package com.dangdang.ddframe.job.api.internal;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowType;

import java.util.Collection;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public interface JobFacade {
    
    /**
     * 获取作业名称.
     *
     * @return 作业名称
     */
    String getJobName();
    
    /**
     * 获取数据流作业类型.
     *
     * @return 数据流作业类型
     */
    DataflowType getDataflowType();
    
    /**
     * 获取同时处理数据的并发线程数.
     *
     * <p>
     * 不能小于1.
     * 仅ThroughputDataflow作业有效.
     * </p>
     *
     * @return 同时处理数据的并发线程数
     */
    int getConcurrentDataProcessThreadCount();
    
    /**
     * 获取是否流式处理数据.
     *
     * <p>
     * 如果流式处理数据, 则fetchData不返回空结果将持续执行作业. 如果非流式处理数据, 则处理数据完成后作业结束.
     * </p>
     *
     * @return 是否流式处理数据
     */
    boolean isStreamingProcess();
    
    /**
     * 获取脚本型作业执行命令行.
     *
     * <p>
     * 仅脚本作业有效.
     * </p>
     *
     * @return 脚本型作业执行命令行
     */
    String getScriptCommandLine();
    
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    void checkMaxTimeDiffSecondsTolerable();
    
    /**
     * 如果需要失效转移, 则设置作业失效转移.
     */
    void failoverIfNecessary();
    
    /**
     * 注册作业启动信息.
     *
     * @param shardingContext 分片上下文
     */
    void registerJobBegin(ShardingContext shardingContext);
    
    /**
     * 注册作业完成信息.
     *
     * @param shardingContext 分片上下文
     */
    void registerJobCompleted(ShardingContext shardingContext);
    
    /**
     * 获取当前作业服务器的分片上下文.
     *
     * @return 分片上下文
     */
    ShardingContext getShardingContext();
    
    /**
     * 设置任务被错过执行的标记.
     *
     * @param shardingItems 需要设置错过执行的任务分片项
     * @return 是否满足misfire条件
     */
    boolean misfireIfNecessary(Collection<Integer> shardingItems);
    
    /**
     * 清除任务被错过执行的标记.
     *
     * @param shardingItems 需要清除错过执行的任务分片项
     */
    void clearMisfire(Collection<Integer> shardingItems);
    
    /**
     * 判断作业是否需要执行错过的任务.
     * 
     * @param shardingItems 任务分片项集合
     * @return 作业是否需要执行错过的任务
     */
    boolean isExecuteMisfired(Collection<Integer> shardingItems);
    
    /**
     * 判断作业是否符合继续运行的条件.
     * 
     * <p>如果作业停止或需要重分片或非流式处理则作业将不会继续运行.</p>
     * 
     * @return 作业是否符合继续运行的条件
     */
    boolean isEligibleForJobRunning();
    
    /**判断是否需要重分片.
     *
     * @return 是否需要重分片
     */
    boolean isNeedSharding();
    
    /**
     * 更新数据处理位置.
     *
     * @param item 分片项
     * @param offset 数据处理位置
     */
    void updateOffset(int item, String offset);
    
    /**
     * 清理作业上次运行时信息.
     * 只会在主节点进行.
     */
    void cleanPreviousExecutionInfo();
    
    /**
     * 作业执行前的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    void beforeJobExecuted(ShardingContext shardingContext);
    
    /**
     * 作业执行后的执行的方法.
     *
     * @param shardingContext 分片上下文
     */
    void afterJobExecuted(ShardingContext shardingContext);
}
