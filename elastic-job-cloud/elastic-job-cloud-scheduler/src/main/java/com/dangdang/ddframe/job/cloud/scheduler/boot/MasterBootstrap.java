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

package com.dangdang.ddframe.job.cloud.scheduler.boot;

import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.MesosConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationListener;
import com.dangdang.ddframe.job.cloud.scheduler.ha.FrameworkIDService;
import com.dangdang.ddframe.job.cloud.scheduler.ha.ReconcileScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.LeasesQueue;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.StatisticsScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.TaskLaunchScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.restful.RestfulService;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action1;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import static com.dangdang.ddframe.job.cloud.scheduler.boot.env.MesosConfiguration.FRAMEWORK_FAILOVER_TIMEOUT;
import static com.dangdang.ddframe.job.cloud.scheduler.boot.env.MesosConfiguration.FRAMEWORK_NAME;

/**
 * Mesos框架启动器.
 *
 * @author zhangliang
 * @author gaohongtao
 */
@Slf4j
@AllArgsConstructor
public final class MasterBootstrap {
    
    private final BootstrapEnvironment env;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final FacadeService facadeService;
    
    private final SchedulerDriver schedulerDriver;
    
    private final ProducerManager producerManager;
    
    private final StatisticManager statisticManager;
    
    private final CloudJobConfigurationListener cloudJobConfigurationListener;
    
    private final Service reconcileScheduledService;
    
    private final Service statisticsScheduledService;
    
    private final Service taskLaunchScheduledService;
    
    private final RestfulService restfulService;
    
    public MasterBootstrap(final CoordinatorRegistryCenter regCenter) {
        env = BootstrapEnvironment.getInstance();
        this.regCenter = regCenter;
        facadeService = new FacadeService(regCenter);
        LeasesQueue leasesQueue = new LeasesQueue();
        TaskScheduler taskScheduler = getTaskScheduler();
        JobEventBus jobEventBus = getJobEventBus();
        schedulerDriver = getSchedulerDriver(leasesQueue, taskScheduler, jobEventBus, new FrameworkIDService(regCenter));
        producerManager = new ProducerManager(schedulerDriver, regCenter);
        statisticManager = StatisticManager.getInstance(regCenter, env.getJobEventRdbConfiguration());
        cloudJobConfigurationListener =  new CloudJobConfigurationListener(regCenter, producerManager);
        reconcileScheduledService = new ReconcileScheduledService(facadeService, schedulerDriver, taskScheduler, statisticManager);
        statisticsScheduledService = new StatisticsScheduledService(regCenter);
        taskLaunchScheduledService = new TaskLaunchScheduledService(leasesQueue, schedulerDriver, taskScheduler, facadeService, jobEventBus);
        restfulService = new RestfulService(regCenter, env.getRestfulServerConfiguration(), producerManager);
    }
    
    private SchedulerDriver getSchedulerDriver(final LeasesQueue leasesQueue, final TaskScheduler taskScheduler,
                                               final JobEventBus jobEventBus, final FrameworkIDService frameworkIDService) {
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder();
        if (frameworkIDOptional.isPresent()) {
            builder.setId(Protos.FrameworkID.newBuilder().setValue(frameworkIDOptional.get()).build());
        }
        Protos.FrameworkInfo frameworkInfo = builder.setUser(mesosConfig.getUser()).setName(FRAMEWORK_NAME)
                .setHostname(mesosConfig.getHostname()).setFailoverTimeout(FRAMEWORK_FAILOVER_TIMEOUT).build();
        return new MesosSchedulerDriver(new SchedulerEngine(leasesQueue, taskScheduler, facadeService, jobEventBus, frameworkIDService, statisticManager), frameworkInfo, mesosConfig.getUrl());
    }
    
    private TaskScheduler getTaskScheduler() {
        return new TaskScheduler.Builder()
                .withLeaseOfferExpirySecs(1000000000L)
                .withLeaseRejectAction(new Action1<VirtualMachineLease>() {
                    
                    @Override
                    public void call(final VirtualMachineLease lease) {
                        log.warn("Declining offer on '{}'", lease.hostname());
                        schedulerDriver.declineOffer(lease.getOffer().getId());
                    }
                }).build();
    }
    
    private JobEventBus getJobEventBus() {
        Optional<JobEventRdbConfiguration> rdbConfig = env.getJobEventRdbConfiguration();
        if (rdbConfig.isPresent()) {
            return new JobEventBus(rdbConfig.get());
        }
        return new JobEventBus();
    }
    
    /**
     * 以守护进程方式启动.
     */
    public void start() {
        facadeService.start();
        schedulerDriver.start();
        producerManager.startup();
        statisticManager.startup();
        cloudJobConfigurationListener.start();
        reconcileScheduledService.startAsync();
        statisticsScheduledService.startAsync();
        taskLaunchScheduledService.startAsync();
        restfulService.start();
    }
    
    /**
     * 停止运行.
     */
    public void stop() {
        restfulService.stop();
        taskLaunchScheduledService.stopAsync();
        statisticsScheduledService.stopAsync();
        reconcileScheduledService.stopAsync();
        cloudJobConfigurationListener.stop();
        statisticManager.shutdown();
        producerManager.shutdown();
        schedulerDriver.stop(true);
        facadeService.stop();
    }
    
}
