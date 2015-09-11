/**
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

package com.dangdang.ddframe.job.internal.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Strings;

/**
 * 弹性化分布式作业配置服务.
 * 
 * @author zhangliang
 */
public final class ConfigurationService {
    
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
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_CLASS, jobNodeStorage.getJobConfiguration().getJobClass().getCanonicalName());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_TOTAL_COUNT, jobNodeStorage.getJobConfiguration().getShardingTotalCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_ITEM_PARAMETERS, jobNodeStorage.getJobConfiguration().getShardingItemParameters());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_PARAMETER, jobNodeStorage.getJobConfiguration().getJobParameter());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CRON, jobNodeStorage.getJobConfiguration().getCron());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_EXECUTION, jobNodeStorage.getJobConfiguration().isMonitorExecution());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS, jobNodeStorage.getJobConfiguration().getProcessCountIntervalSeconds());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, jobNodeStorage.getJobConfiguration().getConcurrentDataProcessThreadCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.FETCH_DATA_COUNT, jobNodeStorage.getJobConfiguration().getFetchDataCount());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.FAILOVER, jobNodeStorage.getJobConfiguration().isFailover());
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ConfigurationNode.DESCRIPTION, jobNodeStorage.getJobConfiguration().getDescription());
    }
    
    /**
     * 获取作业分片总数.
     * 
     * @return 作业分片总数
     */
    public int getShardingTotalCount() {
        return Integer.parseInt(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_TOTAL_COUNT));
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
     * 获取统计作业处理数据数量的间隔时间.
     * 
     * @return 统计作业处理数据数量的间隔时间
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
     * 获取是否开启失效转移.
     * 
     * @return 是否开启失效转移
     */
    public boolean isFailover() {
        return isMonitorExecution() && Boolean.valueOf(jobNodeStorage.getJobNodeData(ConfigurationNode.FAILOVER));
    }
}
