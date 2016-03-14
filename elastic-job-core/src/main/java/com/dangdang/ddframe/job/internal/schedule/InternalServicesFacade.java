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

package com.dangdang.ddframe.job.internal.schedule;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.quartz.JobDataMap;

import java.util.List;

/**
 * 为调度器提供内部服务的门面类.
 * 
 * @author zhangliang
 */
public class InternalServicesFacade {
    
    private final ConfigurationService configService;
    
    private final LeaderElectionService leaderElectionService;
    
    private final ServerService serverService;
    
    private final ShardingService shardingService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final StatisticsService statisticsService;
    
    private final OffsetService offsetService;
    
    private final MonitorService monitorService;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    private final ListenerManager listenerManager;
    
    public InternalServicesFacade(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final List<ElasticJobListener> elasticJobListeners) {
        configService = new ConfigurationService(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
        serverService = new ServerService(coordinatorRegistryCenter, jobConfiguration);
        shardingService = new ShardingService(coordinatorRegistryCenter, jobConfiguration);
        executionContextService = new ExecutionContextService(coordinatorRegistryCenter, jobConfiguration);
        executionService = new ExecutionService(coordinatorRegistryCenter, jobConfiguration);
        failoverService = new FailoverService(coordinatorRegistryCenter, jobConfiguration);
        statisticsService = new StatisticsService(coordinatorRegistryCenter, jobConfiguration);
        offsetService = new OffsetService(coordinatorRegistryCenter, jobConfiguration);
        monitorService = new MonitorService(coordinatorRegistryCenter, jobConfiguration);
        this.elasticJobListeners = elasticJobListeners;
        setGuaranteeServiceForElasticJobListeners(coordinatorRegistryCenter, jobConfiguration);
        listenerManager = new ListenerManager(coordinatorRegistryCenter, jobConfiguration, this.elasticJobListeners);
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        GuaranteeService guaranteeService = new GuaranteeService(coordinatorRegistryCenter, jobConfiguration);
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    /**
     * 注册Elastic-Job启动信息.
     */
    public void registerStartUpInfo() {
        listenerManager.startAllListeners();
        leaderElectionService.leaderElection();
        configService.persistJobConfiguration();
        serverService.persistServerOnline();
        serverService.clearJobStoppedStatus();
        statisticsService.startProcessCountJob();
        shardingService.setReshardingFlag();
        monitorService.listen();
    }
    
    /**
     * 填充作业所需信息.
     * 
     * @param jobDataMap 作业数据字典
     */
    public void fillJobDetail(final JobDataMap jobDataMap) {
        jobDataMap.put("configService", configService);
        jobDataMap.put("shardingService", shardingService);
        jobDataMap.put("executionContextService", executionContextService);
        jobDataMap.put("executionService", executionService);
        jobDataMap.put("failoverService", failoverService);
        jobDataMap.put("offsetService", offsetService);
        jobDataMap.put("elasticJobListeners", elasticJobListeners);
    }
    
    /**
     * 释放作业占用的资源.
     */
    public void releaseJobResource() {
        monitorService.close();
        statisticsService.stopProcessCountJob();
    }
    
    /**
     * 恢复因服务器崩溃而停止的作业信息.
     */
    public void resumeCrashedJobInfo() {
        serverService.persistServerOnline();
        executionService.clearRunningInfo(shardingService.getLocalHostShardingItems());
    }
    
    /**
     * 清除停止作业的标记.
     */
    public void clearJobStoppedStatus() {
        serverService.clearJobStoppedStatus();
    }
    
    /**
     * 判断是否是手工停止的作业.
     *
     * @return 是否是手工停止的作业
     */
    public boolean isJobStoppedManually() {
        return serverService.isJobStoppedManually();
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
