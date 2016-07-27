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

import com.dangdang.ddframe.job.api.type.JobType;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJobConfiguration;
import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
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
    
    public ConfigurationService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final LiteJobConfiguration jobConfiguration) {
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
            String toBeRegisteredJobClassName = jobNodeStorage.getLiteJobConfig().getJobConfig().getJobClass().getCanonicalName();
            if (!toBeRegisteredJobClassName.equals(registeredJobClassName)) {
                throw new JobConflictException(jobNodeStorage.getLiteJobConfig().getJobName(), registeredJobClassName, toBeRegisteredJobClassName);
            }
        }
    }
    
    private void registerJobInfo() {
        fillSimpleJobInfo();
        if (JobType.DATAFLOW == jobNodeStorage.getLiteJobConfig().getJobConfig().getJobType()) {
            fillDataflowJobInfo();
        } else if (JobType.SCRIPT == jobNodeStorage.getLiteJobConfig().getJobConfig().getJobType()) {
            fillScriptJobInfo();
        }
    }
    
    private void fillSimpleJobInfo() {
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_TYPE, jobNodeStorage.getLiteJobConfig().getJobConfig().getJobType());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_CLASS, jobNodeStorage.getLiteJobConfig().getJobConfig().getJobClass().getCanonicalName());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CRON, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().getCron());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_TOTAL_COUNT, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().getShardingTotalCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_ITEM_PARAMETERS, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().getShardingItemParameters());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_PARAMETER, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().getJobParameter());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.FAILOVER, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().isFailover());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MISFIRE, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().isMisfire());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.DESCRIPTION, jobNodeStorage.getLiteJobConfig().getJobConfig().getCoreConfig().getDescription());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MAX_TIME_DIFF_SECONDS, jobNodeStorage.getLiteJobConfig().getMaxTimeDiffSeconds());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_EXECUTION, jobNodeStorage.getLiteJobConfig().isMonitorExecution());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS, jobNodeStorage.getLiteJobConfig().getJobShardingStrategyClass());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_PORT, jobNodeStorage.getLiteJobConfig().getMonitorPort());
    }
    
    private void fillDataflowJobInfo() {
        DataflowJobConfiguration jobConfiguration = (DataflowJobConfiguration) jobNodeStorage.getLiteJobConfig().getJobConfig();
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.DATAFLOW_TYPE, jobConfiguration.getDataflowType());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, jobConfiguration.getConcurrentDataProcessThreadCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.STREAMING_PROCESS, jobConfiguration.isStreamingProcess());
    }
    
    private void fillScriptJobInfo() {
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SCRIPT_COMMAND_LINE, ((ScriptJobConfiguration) jobNodeStorage.getLiteJobConfig().getJobConfig()).getScriptCommandLine());
    }
    
    /**
     * 获取作业类型.
     *
     * @return 作业类型
     */
    public JobType getJobType() {
        return jobNodeStorage.getLiteJobConfig().getJobConfig().getJobType();
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
     * 获取数据流作业类型.
     *
     * @return 数据流作业类型
     */
    public DataflowJobConfiguration.DataflowType getDataflowType() {
        return DataflowJobConfiguration.DataflowType.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.DATAFLOW_TYPE));
    }
    
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
    public int getConcurrentDataProcessThreadCount() {
        return Integer.parseInt(jobNodeStorage.getJobNodeData(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT));
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
        return jobNodeStorage.getLiteJobConfig().getJobName();
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
