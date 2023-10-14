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

package org.apache.shardingsphere.elasticjob.engine.internal.setup;

import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.engine.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.engine.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.engine.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.engine.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.engine.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;

/**
 * Set up facade.
 */
public final class SetUpFacade {
    
    private final LeaderService leaderService;
    
    private final ServerService serverService;
    
    private final InstanceService instanceService;
    
    private final ReconcileService reconcileService;
    
    private final ListenerManager listenerManager;
    
    /**
     * JobName.
     */
    private final String jobName;
    
    /**
     * Registry center.
     */
    private final CoordinatorRegistryCenter regCenter;
    
    public SetUpFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners) {
        leaderService = new LeaderService(regCenter, jobName);
        serverService = new ServerService(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
        reconcileService = new ReconcileService(regCenter, jobName);
        listenerManager = new ListenerManager(regCenter, jobName, elasticJobListeners);
        this.jobName = jobName;
        this.regCenter = regCenter;
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
        if (!reconcileService.isRunning()) {
            reconcileService.startAsync();
        }
        serverService.removeOfflineServers();
    }
    
    /**
     * Tear down.
     */
    public void tearDown() {
        regCenter.removeConnStateListener("/" + this.jobName);
        regCenter.removeDataListeners("/" + this.jobName);
        if (reconcileService.isRunning()) {
            reconcileService.stopAsync();
        }
    }
}
