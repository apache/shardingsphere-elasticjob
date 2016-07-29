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

import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
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
     * 读取作业配置.
     * 
     * @param fromCache 是否从缓存中读取
     * @return 作业配置
     */
    public LiteJobConfiguration load(final boolean fromCache) {
        String configJson = fromCache ? jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT) : jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT);
        return LiteJobConfigurationGsonFactory.getGson().fromJson(configJson, LiteJobConfiguration.class);
    }
    
    /**
     * 持久化分布式作业配置信息.
     */
    public void persist() {
        checkConflictJob();
        if (!jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT) || jobNodeStorage.getLiteJobConfig().isOverwrite()) {
            jobNodeStorage.replaceJobNode(ConfigurationNode.ROOT, LiteJobConfigurationGsonFactory.getGson().toJson(jobNodeStorage.getLiteJobConfig()));
        }
    }
    
    private void checkConflictJob() {
        Optional<LiteJobConfiguration> liteJobConfig = loadInternal();
        if (!liteJobConfig.isPresent()) {
            return;
        }
        if (liteJobConfig.get().getTypeConfig().getJobClass() != jobNodeStorage.getLiteJobConfig().getTypeConfig().getJobClass()) {
            throw new JobConflictException(
                    jobNodeStorage.getLiteJobConfig().getJobName(), liteJobConfig.get().getTypeConfig().getJobClass(), jobNodeStorage.getLiteJobConfig().getTypeConfig().getJobClass());
        }
    }
    
    private Optional<LiteJobConfiguration> loadInternal() {
        if (!jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)) {
            return Optional.absent();
        }
        LiteJobConfiguration result = LiteJobConfigurationGsonFactory.getGson().fromJson(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT), LiteJobConfiguration.class);
        if (null == result) {
            // TODO 应该删除整个job node,并非仅仅删除config node
            jobNodeStorage.removeJobNodeIfExisted(ConfigurationNode.ROOT);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * 获取分片序列号和个性化参数对照表.
     * 
     * @return 分片序列号和个性化参数对照表
     */
    public Map<Integer, String> getShardingItemParameters() {
        String value = load(true).getTypeConfig().getCoreConfig().getShardingItemParameters();
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
     * 检查本机与注册中心的时间误差秒数是否在允许范围.
     */
    public void checkMaxTimeDiffSecondsTolerable() {
        int maxTimeDiffSeconds =  load(true).getMaxTimeDiffSeconds();
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
        LiteJobConfiguration liteJobConfig = load(true);
        return liteJobConfig.isMonitorExecution() && liteJobConfig.getTypeConfig().getCoreConfig().isFailover();
    }
}
