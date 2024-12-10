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

package org.apache.shardingsphere.elasticjob.kernel.executor.facade;

import lombok.extern.slf4j.Slf4j;

import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.spi.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharding Job facade.
 */
@Slf4j
public final class ShardingJobFacade extends AbstractJobFacade {
    
    private final ConfigurationService configService;
    
    private final ShardingService shardingService;
    
    private final ExecutionContextService executionContextService;
    
    private final ExecutionService executionService;
    
    private final FailoverService failoverService;
    
    private final Collection<ElasticJobListener> elasticJobListeners;
    
    private final JobTracingEventBus jobTracingEventBus;
    
    public ShardingJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners, final TracingConfiguration<?> tracingConfig) {
        super(regCenter, jobName, elasticJobListeners, tracingConfig);
        
        configService = new ConfigurationService(regCenter, jobName);
        shardingService = new ShardingService(regCenter, jobName);
        executionContextService = new ExecutionContextService(regCenter, jobName);
        executionService = new ExecutionService(regCenter, jobName);
        failoverService = new FailoverService(regCenter, jobName);
        this.elasticJobListeners = elasticJobListeners.stream().sorted(Comparator.comparingInt(ElasticJobListener::order)).collect(Collectors.toList());
        this.jobTracingEventBus = null == tracingConfig ? new JobTracingEventBus() : new JobTracingEventBus(tracingConfig);
    }
    
    /**
     * Get sharding contexts.
     *
     * @return sharding contexts
     */
    @Override
    public ShardingContexts getShardingContexts() {
        boolean isFailover = configService.load(true).isFailover();
        if (isFailover) {
            List<Integer> failoverShardingItems = failoverService.getLocalFailoverItems();
            if (!failoverShardingItems.isEmpty()) {
                return executionContextService.getJobShardingContext(failoverShardingItems);
            }
        }
        shardingService.shardingIfNecessary();
        List<Integer> shardingItems = shardingService.getLocalShardingItems();
        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalTakeOffItems());
        }
        shardingItems.removeAll(executionService.getDisabledItems(shardingItems));
        return executionContextService.getJobShardingContext(shardingItems);
    }
    
}
