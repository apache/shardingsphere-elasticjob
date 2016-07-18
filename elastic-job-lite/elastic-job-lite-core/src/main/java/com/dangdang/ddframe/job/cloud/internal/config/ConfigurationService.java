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
 */

package com.dangdang.ddframe.job.cloud.internal.config;

import com.dangdang.ddframe.job.cloud.api.config.impl.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.impl.ScriptJobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.impl.JobType;
import com.dangdang.ddframe.job.cloud.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.cloud.exception.JobConflictException;
import com.dangdang.ddframe.job.cloud.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.cloud.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Strings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 弹性化分布式作业配置服务.
 * 
 * @author zhangliang
 * @author caohao
 */
public class ConfigurationService {
    
    private final JobNodeStorage jobNodeStorage;
    
    public ConfigurationService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 持久化分布式作业配置信息.
     */
    public void persistJobConfiguration() {
        checkConflictJob();
        registerJobInfo();
    }
    
    private void checkConflictJob() {
        if (jobNodeStorage.isJobNodeExisted(ConfigurationNode.JOB_CLASS)) {
            String registeredJobClassName = jobNodeStorage.getJobNodeData(ConfigurationNode.JOB_CLASS);
            String toBeRegisteredJobClassName = jobNodeStorage.getJobConfiguration().getJobClass().getCanonicalName();
            if (!toBeRegisteredJobClassName.equals(registeredJobClassName)) {
                throw new JobConflictException(jobNodeStorage.getJobConfiguration().getJobName(), registeredJobClassName, toBeRegisteredJobClassName);
            }
        }
    }
    
    private void registerJobInfo() {
        fillSimpleJobInfo();
        if (JobType.DATA_FLOW == jobNodeStorage.getJobConfiguration().getJobType()) {
            fillDataFlowJobInfo();
        } else if (JobType.SCRIPT == jobNodeStorage.getJobConfiguration().getJobType()) {
            fillScriptJobInfo();
        }
    }
    
    private void fillSimpleJobInfo() {
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_TYPE, jobNodeStorage.getJobConfiguration().getJobType());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_CLASS, jobNodeStorage.getJobConfiguration().getJobClass().getCanonicalName());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_TOTAL_COUNT, jobNodeStorage.getJobConfiguration().getShardingTotalCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_ITEM_PARAMETERS, jobNodeStorage.getJobConfiguration().getShardingItemParameters());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_PARAMETER, jobNodeStorage.getJobConfiguration().getJobParameter());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CRON, jobNodeStorage.getJobConfiguration().getCron());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_EXECUTION, jobNodeStorage.getJobConfiguration().isMonitorExecution());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MAX_TIME_DIFF_SECONDS, jobNodeStorage.getJobConfiguration().getMaxTimeDiffSeconds());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.FAILOVER, jobNodeStorage.getJobConfiguration().isFailover());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MISFIRE, jobNodeStorage.getJobConfiguration().isMisfire());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS, jobNodeStorage.getJobConfiguration().getJobShardingStrategyClass());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.DESCRIPTION, jobNodeStorage.getJobConfiguration().getDescription());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_PORT, jobNodeStorage.getJobConfiguration().getMonitorPort());
    }
    
    private void fillDataFlowJobInfo() {
        DataFlowJobConfiguration jobConfiguration = (DataFlowJobConfiguration) jobNodeStorage.getJobConfiguration();
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS, jobConfiguration.getProcessCountIntervalSeconds());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, jobConfiguration.getConcurrentDataProcessThreadCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.FETCH_DATA_COUNT, jobConfiguration.getFetchDataCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.STREAMING_PROCESS, jobConfiguration.isStreamingProcess());
    }
    
    private void fillScriptJobInfo() {
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SCRIPT_COMMAND_LINE, ((ScriptJobConfiguration) jobNodeStorage.getJobConfiguration()).getScriptCommandLine());
    }
    
    /**
     * 获取作业类型.
     *
     * @return 作业类型
     */
    public JobType getJobType() {
        return jobNodeStorage.getJobConfiguration().getJobType();
    }
    
    /**
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    public int getShardingTotalCount() {
        String result = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_TOTAL_COUNT);
        return Strings.isNullOrEmpty(result) ? -1 : Integer.parseInt(result);
    }
    
    /**
     * 获取分片序列号和个性化参数对照表.
     * 
     * @return 分片序列号和个性化参数对照表
     */
    public Map<Integer, String> getShardingItemParameters() {
        String value = jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS);
        if (Strings.isNullOrEmpty(value)) {
            return Collections.emptyMap();
        }
        String[] shardingItemParameters = value.split(",");
        Map<Integer, String> result = new HashMap<>(shardingItemParameters.length);
        for (String each : shardingItemParameters) {
            String[] pair = each.trim().split("=");
            if (2 != pair.length) {
                throw new ShardingItemParametersException("Sharding item parameters '%s' format error, should be int=xx,int=xx", value);
            }
            try {
                result.put(Integer.parseInt(pair[0].trim()), pair[1].trim());
            } catch (final NumberFormatException ex) {
                throw new ShardingItemParametersException("Sharding item parameters key '%s' is not an integer.", pair[0]);
            }
        }
        return result;
    }
    
    /**
     * 获取作业自定义参数.
     * 
     * @return 作业自定义参数
     */
    public String getJobParameter() {
        return jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.JOB_PARAMETER);
    }
    
    /**
     * 获取作业启动时间的cron表达式.
     * 
     * @return 作业启动时间的cron表达式
     */
    public String getCron() {
        return jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.CRON);
    }
    
    /**
     * 获取是否监控作业运行时状态.
     * 
     * @return 是否监控作业运行时状态
     */
    public boolean isMonitorExecution() {
        return Boolean.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_EXECUTION));
    }
    
    /**
     * 获取统计作业处理数据数量的间隔时间.
     * 
     * @return 统计作业处理数据数量的间隔时间
     */
    public int getProcessCountIntervalSeconds() {
        return Integer.parseInt(jobNodeStorage.getJobNodeData(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS));
    }
    
    /**
     * 获取同时处理数据的并发线程数.
     * 
     * <p>
     * 不能小于1.
     * 仅ThroughputDataFlow作业有效.
     * </p>
     * 
     * @return 同时处理数据的并发线程数
     */
    public int getConcurrentDataProcessThreadCount() {
        return Integer.parseInt(jobNodeStorage.getJobNodeData(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT));
    }
    
    /**
     * 获取每次抓取的数据量.
     * 
     * @return 每次抓取的数据量
     */
    public int getFetchDataCount() {
        return Integer.parseInt(jobNodeStorage.getJobNodeData(ConfigurationNode.FETCH_DATA_COUNT));
    }
    
    /**
     * 获取是否流式处理数据.
     *
     * @return 是否流式处理数据
     */
    public boolean isStreamingProcess() {
        return Boolean.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.STREAMING_PROCESS));
    }
    
    /**
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        int maxTimeDiffSeconds =  Integer.parseInt(jobNodeStorage.getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS));
        if (-1  == maxTimeDiffSeconds) {
            return;
        }
        long timeDiff = Math.abs(System.currentTimeMillis() - jobNodeStorage.getRegistryCenterTime());
        if (timeDiff > maxTimeDiffSeconds * 1000L) {
            throw new TimeDiffIntolerableException(Long.valueOf(timeDiff / 1000).intValue(), maxTimeDiffSeconds);
        }
    }
    
    /**
     * 获取是否开启失效转移.
     * 
     * @return 是否开启失效转移
     */
    public boolean isFailover() {
        return isMonitorExecution() && Boolean.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.FAILOVER));
    }
    
    /**
     * 获取是否开启misfire.
     * 
     * @return 是否开启misfire
     */
    public boolean isMisfire() {
        return Boolean.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.MISFIRE));
    }
    
    /**
     * 获取作业分片策略实现类全路径.
     * 
     * @return 作业分片策略实现类全路径
     */
    public String getJobShardingStrategyClass() {
        return jobNodeStorage.getJobNodeData(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS);
    }
    
    /**
     * 获取作业监控端口.
     * 
     * @return 作业监控端口
     */
    public int getMonitorPort() {
        return Integer.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_PORT));
    }
    
    /**
     * 获取作业名称.
     * 
     * @return 作业名称
     */
    public String getJobName() {
        return jobNodeStorage.getJobConfiguration().getJobName();
    }
    
    /**
     * 获取作业执行脚本命令行.
     *
     * <p>
     * 仅脚本型作业有效.
     * </p>
     *
     * @return 脚本型作业执行脚本命令行
     */
    public String getScriptCommandLine() {
        return jobNodeStorage.getJobNodeData(ConfigurationNode.SCRIPT_COMMAND_LINE);
    }
}
