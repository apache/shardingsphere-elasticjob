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

package com.dangdang.ddframe.job.cloud.internal.schedule;

import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.cloud.api.config.impl.JobType;
import com.dangdang.ddframe.job.cloud.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.cloud.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.cloud.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.cloud.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.cloud.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.cloud.internal.server.ServerService;
import com.dangdang.ddframe.job.cloud.internal.statistics.StatisticsService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

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
    
    private final StatisticsService statisticsService;
    
    private final MonitorService monitorService;
    
    private final ListenerManager listenerManager;
    
    public SchedulerFacade(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final List<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        statisticsService = new StatisticsService(coordinatorRegistryCenter, jobConfiguration);
        monitorService = new MonitorService(coordinatorRegistryCenter, jobConfiguration);
        listenerManager = new ListenerManager(coordinatorRegistryCenter, jobConfiguration, elasticJobListeners);
    }
    
    /**
     * 每次作业启动前清理上次运行状态.
     */
    public void clearPreviousServerStatus() {
        serverService.clearPreviousServerStatus();
    }
    
    /**
     * 注册Elastic-Job启动信息.
     */
    public void registerStartUpInfo() {
        listenerManager.startAllListeners();
        leaderElectionService.leaderForceElection();
        configService.persistJobConfiguration();
        serverService.persistServerOnline();
        serverService.clearJobPausedStatus();
        if (JobType.DATA_FLOW == configService.getJobType()) {
            statisticsService.startProcessCountJob();
        }
        shardingService.setReshardingFlag();
        monitorService.listen();
    }
    
    /**
     * 释放作业占用的资源.
     */
    public void releaseJobResource() {
        monitorService.close();
        if (JobType.DATA_FLOW.equals(configService.getJobType())) {
            statisticsService.stopProcessCountJob();
        }
        serverService.removeServerStatus();
    }
    
    /**
     * 获取作业启动时间的cron表达式.
     *
     * @return 作业启动时间的cron表达式
     */
    public String getCron() {
        return configService.getCron();
    }
    
    /**
     * 获取是否开启misfire.
     *
     * @return 是否开启misfire
     */
    public boolean isMisfire() {
        return configService.isMisfire();
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
