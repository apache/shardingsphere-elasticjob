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

package com.dangdang.ddframe.job.executor;

import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.exception.JobExecutionEnvironmentException;

import java.util.Collection;

/**
 * 作业内部服务门面服务.
 * 
 * @author zhangliang
 */
public interface JobFacade {
    
    /**
     * 读取作业配置.
     * 
     * @param fromCache 是否从缓存中读取
     * @return 作业配置
     */
    JobRootConfiguration loadJobRootConfiguration(boolean fromCache);
    
    /**
     * 检查作业执行环境.
     * 
     * @throws JobExecutionEnvironmentException 作业执行环境异常
     */
    void checkJobExecutionEnvironment() throws JobExecutionEnvironmentException;
    
    /**
     * 如果需要失效转移, 则执行作业失效转移.
     */
    void failoverIfNecessary();
    
    /**
     * 注册作业启动信息.
     *
     * @param shardingContexts 分片上下文
     */
    void registerJobBegin(ShardingContexts shardingContexts);
    
    /**
     * 注册作业完成信息.
     *
     * @param shardingContexts 分片上下文
     */
    void registerJobCompleted(ShardingContexts shardingContexts);
    
    /**
     * 获取当前作业服务器的分片上下文.
     *
     * @return 分片上下文
     */
    ShardingContexts getShardingContexts();
    
    /**
     * 设置任务被错过执行的标记.
     *
     * @param shardingItems 需要设置错过执行的任务分片项
     * @return 是否满足misfire条件
     */
    boolean misfireIfRunning(Collection<Integer> shardingItems);
    
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
     * 作业执行前的执行的方法.
     *
     * @param shardingContexts 分片上下文
     */
    void beforeJobExecuted(ShardingContexts shardingContexts);
    
    /**
     * 作业执行后的执行的方法.
     *
     * @param shardingContexts 分片上下文
     */
    void afterJobExecuted(ShardingContexts shardingContexts);
    
    /**
     * 发布执行事件.
     *
     * @param jobExecutionEvent 作业执行事件
     */
    void postJobExecutionEvent(JobExecutionEvent jobExecutionEvent);
    
    /**
     * 发布作业状态追踪事件.
     *
     * @param taskId 作业Id
     * @param state 作业执行状态
     * @param message 作业执行消息
     */
    void postJobStatusTraceEvent(String taskId, JobStatusTraceEvent.State state, String message);
}
