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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.lite.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.reconcile.ReconcileService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public class SchedulerFacade {
    
    private final ConfigurationService configService;
    
    private final LeaderElectionService leaderElectionService;
    
    private final ServerService serverService;
    
    private final ShardingService shardingService;
    
    private final ExecutionService executionService;
    
    private final MonitorService monitorService;
    
    private final ListenerManager listenerManager;
    
    private final ReconcileService reconcileService;
    
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(regCenter, jobName);
        leaderElectionService = new LeaderElectionService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        monitorService = new MonitorService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
        listenerManager = new ListenerManager(regCenter, jobName, elasticJobListeners);
    }
    
    /**
     * 每次作业启动前清理上次运行状态.
     */
    public void clearPreviousServerStatus() {
        serverService.clearPreviousServerStatus();
    }
    
    /**
     * 注册Elastic-Job启动信息.
     * 
     * @param liteJobConfig 作业配置
     */
    public void registerStartUpInfo(final LiteJobConfiguration liteJobConfig) {
        listenerManager.startAllListeners();
        leaderElectionService.leaderForceElection();
        configService.persist(liteJobConfig);
        serverService.persistServerOnline(!liteJobConfig.isDisabled());
        serverService.clearJobPausedStatus();
        shardingService.setReshardingFlag();
        monitorService.listen();
        listenerManager.setCurrentShardingTotalCount(configService.load(false).getTypeConfig().getCoreConfig().getShardingTotalCount());
        reconcileService.startAsync();
    }
    
    /**
     * 释放作业占用的资源.
     */
    public void releaseJobResource() {
        monitorService.close();
        serverService.removeServerStatus();
    }
    
    /**
     * 读取作业配置.
     *
     * @return 作业配置
     */
    public LiteJobConfiguration loadJobConfiguration() {
        return configService.load(false);
    }
    
    /**
     * 获取作业触发监听器.
     * 
     * @return 作业触发监听器
     */
    public JobTriggerListener newJobTriggerListener() {
        return new JobTriggerListener(executionService, shardingService);
    }
}
