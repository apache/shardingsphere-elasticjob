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
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationNode;
import com.dangdang.ddframe.job.cloud.scheduler.ha.FrameworkIDService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.LeasesQueue;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.StatisticsScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.TaskLaunchScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudJobRestfulApi;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action1;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.concurrent.Executors;

/**
 * Mesos框架启动器.
 *
 * @author zhangliang
 * @author gaohongtao
 */
@Slf4j
public final class MasterBootstrap {
    
    private static final double ONE_WEEK_TIMEOUT = 60 * 60 * 24 * 7;
    
    private final BootstrapEnvironment env;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final FacadeService facadeService;
    
    private final RestfulServer restfulServer;
    
    private final FrameworkIDService frameworkIDService;
    
    private SchedulerDriver schedulerDriver;
    
    private ProducerManager producerManager;
    
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    private Service taskLaunchScheduledService;
    
    private Service statisticsScheduledService;
    
    public MasterBootstrap() {
        env = BootstrapEnvironment.getInstance();
        regCenter = getRegistryCenter();
        facadeService = new FacadeService(regCenter);
        restfulServer = new RestfulServer(env.getRestfulServerConfiguration().getPort());
        frameworkIDService = new FrameworkIDService(regCenter);
        CloudJobRestfulApi.init(regCenter);
        Runtime.getRuntime().addShutdownHook(new Thread("master-bootstrap-shutdown-hook") {
            
            @Override
            public void run() {
                facadeService.stop();
            }
        });
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(env.getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    /**
     * 以守护进程方式启动.
     * 
     * @throws Exception 启动异常
     */
    public void start() throws Exception {
        LeasesQueue leasesQueue = new LeasesQueue();
        TaskScheduler taskScheduler = getTaskScheduler();
        JobEventBus jobEventBus = getJobEventBus();
        schedulerDriver = getSchedulerDriver(leasesQueue, taskScheduler, facadeService, jobEventBus);
        producerManager = new ProducerManager(schedulerDriver, regCenter);
        producerManager.startup();
        CloudJobRestfulApi.setContext(schedulerDriver, producerManager);
        log.info("Elastic Job: Add configuration listener");
        cloudJobConfigurationListener =  new CloudJobConfigurationListener(schedulerDriver, producerManager, regCenter);
        getCache().getListenable().addListener(cloudJobConfigurationListener, Executors.newSingleThreadExecutor());
        taskLaunchScheduledService = new TaskLaunchScheduledService(leasesQueue, schedulerDriver, taskScheduler, facadeService, jobEventBus).startAsync();
        statisticsScheduledService = new StatisticsScheduledService().startAsync();
        restfulServer.start(CloudJobRestfulApi.class.getPackage().getName());
        schedulerDriver.start();
    }
    
    private SchedulerDriver getSchedulerDriver(final LeasesQueue leasesQueue, final TaskScheduler taskScheduler, final FacadeService facadeService, final JobEventBus jobEventBus) {
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Optional<String> frameworkIDOptional = frameworkIDService.fetch();
        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder();
        if (frameworkIDOptional.isPresent()) {
            builder.setId(Protos.FrameworkID.newBuilder().setValue(frameworkIDOptional.get()).build());
        }
        Protos.FrameworkInfo frameworkInfo = builder.setUser(mesosConfig.getUser()).setName(MesosConfiguration.FRAMEWORK_NAME)
                        .setHostname(mesosConfig.getHostname()).setFailoverTimeout(ONE_WEEK_TIMEOUT).build();
        return new MesosSchedulerDriver(new SchedulerEngine(leasesQueue, taskScheduler, facadeService, jobEventBus, frameworkIDService), frameworkInfo, mesosConfig.getUrl());
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
    
    private TreeCache getCache() {
        TreeCache result = (TreeCache) regCenter.getRawCache(ConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        regCenter.addCacheData(ConfigurationNode.ROOT);
        return (TreeCache) regCenter.getRawCache(ConfigurationNode.ROOT);
    }
    
    /**
     * 停止运行.
     */
    public synchronized void stop() {
        if (null != taskLaunchScheduledService) {
            taskLaunchScheduledService.stopAsync();
        }
        if (null != statisticsScheduledService) {
            statisticsScheduledService.stopAsync();
        }
        restfulServer.stop();
        if (null != cloudJobConfigurationListener) {
            log.info("Elastic Job: Remove configuration listener");
            getCache().getListenable().removeListener(cloudJobConfigurationListener);
        }
        if (null != producerManager) {
            producerManager.shutdown();
        }
        if (null != schedulerDriver) {
            schedulerDriver.stop(true);
        }
    }
}
