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

package com.dangdang.ddframe.job.cloud.scheduler.framework;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.LeasesQueue;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.TaskLaunchScheduledService;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action1;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.SchedulerDriver;

/**
 * 调度器上下文.
 * 
 * @author gaohongtao
 */
@Slf4j
@Getter
public class MesosSchedulerContext implements AutoCloseable {
    
    private final LeasesQueue leasesQueue = new LeasesQueue();
    
    private final FacadeService facadeService;
    
    private final JobEventBus jobEventBus;
    
    private TaskScheduler taskScheduler;
    
    private TaskLaunchScheduledService taskLaunchScheduledService;
    
    @Setter(AccessLevel.PACKAGE)
    private AbstractFramework delegate;
    
    @Setter(AccessLevel.PACKAGE)
    private SchedulerDriver schedulerDriver;
    
    MesosSchedulerContext(final CoordinatorRegistryCenter registryCenter) {
        facadeService = new FacadeService(registryCenter);
        jobEventBus = newJobEventBus();
    }
    
    private JobEventBus newJobEventBus() {
        Optional<JobEventRdbConfiguration> rdbConfig = BootstrapEnvironment.getInstance().getJobEventRdbConfiguration();
        if (rdbConfig.isPresent()) {
            return new JobEventBus(rdbConfig.get());
        }
        return new JobEventBus();
    }
    
    /**
     * 执行注册完成后的动作.
     */
    public void doRegistered() {
        taskScheduler = new TaskScheduler.Builder()
                .withLeaseOfferExpirySecs(1000000000L)
                .withLeaseRejectAction(new Action1<VirtualMachineLease>() {
                
                    @Override
                    public void call(final VirtualMachineLease lease) {
                        log.warn("Elastic job: Declining offer on '{}'", lease.hostname());
                        schedulerDriver.declineOffer(lease.getOffer().getId());
                    }
                }).build();
        facadeService.start();
        taskLaunchScheduledService = new TaskLaunchScheduledService(this, schedulerDriver);
        try {
            Frameworks.invoke("Start taskLaunchScheduledService", new Frameworks.Invokable() {
                @Override
                public void invoke() throws Exception {
                    taskLaunchScheduledService.startAsync();
                    taskLaunchScheduledService.awaitRunning();
                }
            });
            delegate.start();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            HAFramework.interrupt();
        }
    }
    
    /**
     * 执行断线后动作.
     */
    public void doDisconnect() {
        HAFramework.interrupt();
    }
    
    @Override
    public void close() {
        taskScheduler.shutdown();
        facadeService.stop();
        Frameworks.safetyInvoke("Stop taskLaunchScheduledService", new Frameworks.Invokable() {
            @Override
            public void invoke() throws Exception {
                taskLaunchScheduledService.stopAsync();
                taskLaunchScheduledService.awaitTerminated();
            }
        });
    }
}
