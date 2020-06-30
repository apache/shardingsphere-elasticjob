/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.monitor.MonitorService;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * Scheduler facade.
 */
public final class SchedulerFacade {
    
    private final String jobName;
    
    private final ConfigurationService configService;
    
    private final LeaderService leaderService;
    
    private final ServerService serverService;
    
    private final InstanceService instanceService;
    
    private final ShardingService shardingService;
    
    private final ExecutionService executionService;
    
    private final MonitorService monitorService;
    
    private final ReconcileService reconcileService;
    
    private ListenerManager listenerManager;
    
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        monitorService = new MonitorService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
    }
    
    public SchedulerFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final List<ElasticJobListener> elasticJobListeners) {
        this.jobName = jobName;
        configService = new ConfigurationService(regCenter, jobName);
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        monitorService = new MonitorService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
        listenerManager = new ListenerManager(regCenter, jobName, elasticJobListeners);
    }
    
    /**
     * Create job trigger listener.
     *
     * @return job trigger listener
     */
    public JobTriggerListener newJobTriggerListener() {
        return new JobTriggerListener(executionService, shardingService);
    }
    
    /**
     * Update job configuration.
     *
     * @param jobClassName job class name
     * @param jobConfig job configuration to be updated
     * @return updated job configuration
     */
    public JobConfiguration updateJobConfiguration(final String jobClassName, final JobConfiguration jobConfig) {
        configService.setUpJobConfiguration(jobClassName, jobConfig);
        return configService.load(false);
    }
    
    /**
     * Register start up info.
     * 
     * @param enabled enable job on startup
     */
    public void registerStartUpInfo(final boolean enabled) {
        listenerManager.startAllListeners();
        leaderService.electLeader();
        serverService.persistOnline(enabled);
        instanceService.persistOnline();
        shardingService.setReshardingFlag();
        monitorService.listen();
        if (!reconcileService.isRunning()) {
            reconcileService.startAsync();
        }
    }
    
    /**
     * Shutdown instance.
     */
    public void shutdownInstance() {
        if (leaderService.isLeader()) {
            leaderService.removeLeader();
        }
        monitorService.close();
        if (reconcileService.isRunning()) {
            reconcileService.stopAsync();
        }
        JobRegistry.getInstance().shutdown(jobName);
    }
}
