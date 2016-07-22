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

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;

/**
 * Elastic Job配置根节点名称的常量类.
 * 
 * @author zhangliang
 */
public final class ConfigurationNode {
    
    private static final String ROOT = "config";
    
    static final String JOB_TYPE = ROOT + "/jobType";
    
    static final String JOB_CLASS = ROOT + "/jobClass";
    
    static final String SHARDING_TOTAL_COUNT = ROOT + "/shardingTotalCount";
    
    static final String CRON = ROOT + "/cron";
    
    static final String SHARDING_ITEM_PARAMETERS = ROOT + "/shardingItemParameters";
    
    static final String JOB_PARAMETER = ROOT + "/jobParameter";
    
    static final String MONITOR_EXECUTION = ROOT + "/monitorExecution";
    
    static final String PROCESS_COUNT_INTERVAL_SECONDS = ROOT + "/processCountIntervalSeconds";
    
    static final String CONCURRENT_DATA_PROCESS_THREAD_COUNT = ROOT + "/concurrentDataProcessThreadCount";
    
    static final String STREAMING_PROCESS = ROOT + "/streamingProcess";
    
    static final String MAX_TIME_DIFF_SECONDS = ROOT + "/maxTimeDiffSeconds";
    
    static final String FAILOVER = ROOT + "/failover";
    
    static final String MISFIRE = ROOT + "/misfire";
    
    static final String JOB_SHARDING_STRATEGY_CLASS = ROOT + "/jobShardingStrategyClass";
    
    static final String DESCRIPTION = ROOT + "/description";
    
    static final String MONITOR_PORT = ROOT + "/monitorPort";

    static final String SCRIPT_COMMAND_LINE = ROOT + "/scriptCommandLine";
    
    private final JobNodePath jobNodePath;
    
    public ConfigurationNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * 判断是否为作业分片总数路径.
     * 
     * @param path 节点路径
     * @return 是否为作业分片总数路径
     */
    public boolean isShardingTotalCountPath(final String path) {
        return jobNodePath.getFullPath(SHARDING_TOTAL_COUNT).equals(path);
    }
    
    /**
     * 判断是否为监控作业执行时状态路径.
     * 
     * @param path 节点路径
     * @return 是否为监控作业执行时状态路径
     */
    public boolean isMonitorExecutionPath(final String path) {
        return jobNodePath.getFullPath(MONITOR_EXECUTION).equals(path);
    }
    
    /**
     * 判断是否为失效转移设置路径.
     * 
     * @param path 节点路径
     * @return 是否为失效转移设置路径
     */
    public boolean isFailoverPath(final String path) {
        return jobNodePath.getFullPath(FAILOVER).equals(path);
    }
    
    /**
     * 判断是否为作业调度配置路径.
     * 
     * @param path 节点路径
     * @return 是否为作业调度配置路径
     */
    public boolean isCronPath(final String path) {
        return jobNodePath.getFullPath(CRON).equals(path);
    }
}
