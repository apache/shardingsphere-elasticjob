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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.spi.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;

import lombok.extern.slf4j.Slf4j;

/**
 * Single Sharding Job facade.
 */
@Slf4j
public final class SingleShardingJobFacade extends AbstractJobFacade {

    private final JobNodeStorage jobNodeStorage;
    private final InstanceService instanceService;

    public SingleShardingJobFacade(final CoordinatorRegistryCenter regCenter, final String jobName, final Collection<ElasticJobListener> elasticJobListeners, final TracingConfiguration<?> tracingConfig) {
        super(regCenter, jobName, elasticJobListeners, tracingConfig);

        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        instanceService = new InstanceService(regCenter, jobName);
    }

    @Override
    public void registerJobCompleted(final ShardingContexts shardingContexts) {
        super.registerJobCompleted(shardingContexts);

        JobConfiguration jobConfig = configService.load(true);
        JobInstance jobInst = JobRegistry.getInstance().getJobInstance(jobConfig.getJobName());
        if (null == jobInst) {
            log.warn("Error! Can't find the job instance with name:{}", jobConfig.getJobName());
            return;
        }
        Integer nextIndex = null;
        List<JobInstance> availJobInst = instanceService.getAvailableJobInstances();
        for (int i = 0; i < availJobInst.size(); i++) {
            JobInstance temp = availJobInst.get(i);
            if (temp.getServerIp().equals(jobInst.getServerIp())) {
                nextIndex = i + 1; // find the current running job instance, and set next one to current index + 1
                break;
            }
        }
        if (nextIndex != null) { // the normal case that can find the next index, exclude the bounded scenarios
            nextIndex = nextIndex >= availJobInst.size() ? 0 : nextIndex; // Round Robin Loop
            jobNodeStorage.fillEphemeralJobNode("next-job-instance-ip", availJobInst.get(nextIndex).getServerIp());
        }

        if (log.isDebugEnabled()) {
            log.debug("job name: {}, next index: {}, sharding total count: {}",
                jobConfig.getJobName(), nextIndex, jobConfig.getShardingTotalCount());
        }
    }

    /**
     * Get sharding contexts.
     *
     * @return sharding contexts
     */
    @Override
    public ShardingContexts getShardingContexts() {
        JobConfiguration jobConfig = configService.load(true);
        boolean isFailover = jobConfig.isFailover();
        if (isFailover) {
            List<Integer> failoverShardingItems = failoverService.getLocalFailoverItems();
            if (!failoverShardingItems.isEmpty()) {
                return executionContextService.getJobShardingContext(failoverShardingItems);
            }
        }

        List<Integer> shardingItems;
        String nextJobInstIP = null;
        if (isNeedSharding()) { // the first initialization or reconcile case
            shardingService.shardingIfNecessary();
            shardingItems = shardingService.getLocalShardingItems();
        } else {
            nextJobInstIP = jobNodeStorage.getJobNodeDataDirectly("next-job-instance-ip");
            if (StringUtils.isBlank(nextJobInstIP)) { // if there is no next job instance ip
                shardingService.shardingIfNecessary();
                shardingItems = shardingService.getLocalShardingItems();
            } else { // when next job instance is specified under normal case
                JobInstance jobInst = JobRegistry.getInstance().getJobInstance(jobConfig.getJobName());
                shardingItems = nextJobInstIP.equals(jobInst.getServerIp()) ? Collections.singletonList(0) : new ArrayList<>();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("job name: {}, sharding items: {}, nextJobInstIP: {}, sharding total count: {}, isFailover: {}",
                jobConfig.getJobName(), shardingItems, nextJobInstIP, jobConfig.getShardingTotalCount(), isFailover);
        }

        if (isFailover) {
            shardingItems.removeAll(failoverService.getLocalTakeOffItems());
        }
        shardingItems.removeAll(executionService.getDisabledItems(shardingItems));
        return executionContextService.getJobShardingContext(shardingItems);
    }

}
