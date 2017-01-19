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
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.LeasesQueue;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.StatisticsProcessor;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.TaskLaunchProcessor;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManagerFactory;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudAppRestfulApi;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudJobRestfulApi;
import com.dangdang.ddframe.job.cloud.scheduler.statistics.StatisticManager;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.restful.RestfulServer;
import com.google.common.base.Optional;
import com.netflix.fenzo.TaskScheduler;
import com.netflix.fenzo.VirtualMachineLease;
import com.netflix.fenzo.functions.Action1;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * Mesos框架启动器.
 *
 * @author zhangliang
 */
@Slf4j
public final class MasterBootstrap {
    
    private static final String CONSOLE_PATH = "console";
    
    private final BootstrapEnvironment env;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final SchedulerDriver schedulerDriver;
    
    private final RestfulServer restfulServer;
    
    public MasterBootstrap() {
        env = BootstrapEnvironment.getInstance();
        regCenter = getRegistryCenter();
        LeasesQueue leasesQueue = new LeasesQueue();
        final FacadeService facadeService = new FacadeService(regCenter);
        TaskScheduler taskScheduler = getTaskScheduler();
        JobEventBus jobEventBus = getJobEventBus();
        final StatisticManager statisticManager = StatisticManager.getInstance(regCenter, env.getJobEventRdbConfiguration());
        statisticManager.startup();
        schedulerDriver = getSchedulerDriver(leasesQueue, taskScheduler, facadeService, jobEventBus, statisticManager);
        restfulServer = new RestfulServer(env.getRestfulServerConfiguration().getPort());
        CloudJobRestfulApi.init(schedulerDriver, regCenter);
        CloudAppRestfulApi.init(regCenter);
        initConfigurationListener();
        final ProducerManager producerManager = ProducerManagerFactory.getInstance(schedulerDriver, regCenter);
        producerManager.startup();
        new Thread(new TaskLaunchProcessor(leasesQueue, schedulerDriver, taskScheduler, facadeService, jobEventBus), "task-launch-processor-" + Thread.currentThread().getId()).start();
        new Thread(new StatisticsProcessor(), "statistics-processor-" + Thread.currentThread().getId()).start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            
            @Override
            public void run() {
                facadeService.stop();
                producerManager.shutdown();
                statisticManager.shutdown();
            }
        });
    }
    
    private CoordinatorRegistryCenter getRegistryCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(env.getZookeeperConfiguration());
        result.init();
        return result;
    }
    
    private SchedulerDriver getSchedulerDriver(
            final LeasesQueue leasesQueue, final TaskScheduler taskScheduler, final FacadeService facadeService, final JobEventBus jobEventBus, final StatisticManager statisticManager) {
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Protos.FrameworkInfo frameworkInfo = 
                Protos.FrameworkInfo.newBuilder().setUser(mesosConfig.getUser()).setName(MesosConfiguration.FRAMEWORK_NAME).setHostname(mesosConfig.getHostname()).build();
        return new MesosSchedulerDriver(new SchedulerEngine(leasesQueue, taskScheduler, facadeService, jobEventBus, statisticManager), frameworkInfo, mesosConfig.getUrl());
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
    
    private void initConfigurationListener() {
        regCenter.addCacheData(ConfigurationNode.ROOT);
        ((TreeCache) regCenter.getRawCache(ConfigurationNode.ROOT)).getListenable().addListener(new CloudJobConfigurationListener(schedulerDriver, regCenter));
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
     * 
     * @return 框架运行状态
     * @throws Exception 运行时异常
     */
    public Protos.Status runAsDaemon() throws Exception {
        restfulServer.start(CloudJobRestfulApi.class.getPackage().getName(), Optional.of(CONSOLE_PATH));
        return schedulerDriver.run();
    }
    
    /**
     * 停止运行.
     * 
     * @param status 框架运行状态
     * @return 是否正常停止
     * @throws Exception 运行时异常
     */
    public boolean stop(final Protos.Status status) throws Exception {
        schedulerDriver.stop();
        restfulServer.stop();
        return Protos.Status.DRIVER_STOPPED == status;
    }
}
