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

package org.apache.shardingsphere.elasticjob.kernel.internal.reconcile;

import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.concurrent.TimeUnit;

/**
 * Reconcile service.
 */
@Slf4j
public final class ReconcileService extends AbstractScheduledService {
    
    private long lastReconcileTime;
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final JobNodePath jobNodePath;
    
    private final CoordinatorRegistryCenter regCenter;
    
    public ReconcileService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.regCenter = regCenter;
        lastReconcileTime = System.currentTimeMillis();
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        jobNodePath = new JobNodePath(jobName);
    }
    
    @Override
    protected void runOneIteration() {
        int reconcileIntervalMinutes = configService.load(true).getReconcileIntervalMinutes();
        if (reconcileIntervalMinutes > 0 && System.currentTimeMillis() - lastReconcileTime >= (long) reconcileIntervalMinutes * 60 * 1000) {
            lastReconcileTime = System.currentTimeMillis();
            if (!shardingService.isNeedSharding() && shardingService.hasShardingInfoInOfflineServers() && !(isStaticSharding() && hasShardingInfo())) {
                log.warn("Elastic Job: job status node has inconsistent value,start reconciling...");
                shardingService.setReshardingFlag();
            }
        }
    }
    
    private boolean isStaticSharding() {
        return configService.load(true).isStaticSharding();
    }
    
    private boolean hasShardingInfo() {
        return !regCenter.getChildrenKeys(jobNodePath.getShardingNodePath()).isEmpty();
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.MINUTES);
    }
}
