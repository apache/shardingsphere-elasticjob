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

package com.dangdang.ddframe.job.cloud.scheduler.container;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfigurationListener;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationNode;
import com.dangdang.ddframe.job.cloud.scheduler.boot.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.FacadeService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.LeasesQueue;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.SchedulerEngine;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.StatisticsScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.TaskLaunchScheduledService;
import com.dangdang.ddframe.job.cloud.scheduler.restful.CloudJobRestfulApi;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
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

import java.util.concurrent.Executors;

/**
 * 单机运行容器.
 * 
 * @author zhangliang
 * @author gaohongtao
 */
@Slf4j
class StandaloneFrameworkContainer extends AbstractFrameworkContainer {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final Protos.FrameworkInfo frameworkInfo;
    
    private final FacadeService facadeService;
    
    private final RestfulServer restfulServer;
    
    private final AbstractFrameworkContainer wrapper;
    
    private volatile TaskLaunchScheduledService taskLaunchScheduledService;
    
    private volatile LeasesQueue leasesQueue;
    
    private volatile TaskScheduler taskScheduler;
    
    private volatile JobEventBus jobEventBus;
    
    private volatile SchedulerDriver schedulerDriver;
    
    private volatile CloudJobConfigurationListener cloudJobConfigurationListener;
    
    StandaloneFrameworkContainer(final CoordinatorRegistryCenter regCenter, final Protos.FrameworkInfo frameworkInfo, final AbstractFrameworkContainer wrapper) {
        super(regCenter);
        this.frameworkInfo = frameworkInfo;
        this.wrapper = null == wrapper ? this : wrapper;
        facadeService = new FacadeService(getRegCenter());
        restfulServer = new RestfulServer(env.getRestfulServerConfiguration().getPort());
        CloudJobRestfulApi.init(getRegCenter());
        new StatisticsScheduledService().startAsync();
    }
    
    @Override
    public void start() throws Exception {
        leasesQueue = new LeasesQueue();
        taskScheduler = getTaskScheduler();
        jobEventBus = getJobEventBus();
        schedulerDriver = new MesosSchedulerDriver(new SchedulerEngine(leasesQueue, taskScheduler, facadeService, jobEventBus, wrapper), frameworkInfo, env.getMesosConfiguration().getUrl());
        invoke("Start driver", new Invokable() {
            @Override
            public void invoke() throws Exception {
                schedulerDriver.start();
            }
        });
    }
    
    private TaskScheduler getTaskScheduler() {
        return new TaskScheduler.Builder()
                .withLeaseOfferExpirySecs(1000000000L)
                .withLeaseRejectAction(new Action1<VirtualMachineLease>() {
                    
                    @Override
                    public void call(final VirtualMachineLease lease) {
                        log.warn("Elastic job: Declining offer on '{}'", lease.hostname());
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
    
    private void invoke(final String message, final Invokable invokable) throws Exception {
        log.info("Elastic job: {}", message);
        try {
            invokable.invoke();
            //CHECKSTYLE:OFF
        } catch (final Exception ex) {
            //CHECKSTYLE:ON
            log.error("Elastic job: {} error", message, ex);
            throw ex;
        }
    }
    
    @Override
    public synchronized void resume() {
        taskScheduler.expireAllLeases();
        facadeService.start();
        CloudJobRestfulApi.start(schedulerDriver);
        cloudJobConfigurationListener =  new CloudJobConfigurationListener(schedulerDriver, getRegCenter());
        getCache().getListenable().addListener(cloudJobConfigurationListener, Executors.newSingleThreadExecutor());
        taskLaunchScheduledService = new TaskLaunchScheduledService(leasesQueue, schedulerDriver, taskScheduler, facadeService, jobEventBus);
        safetyInvoke("Start taskLaunchScheduledService", new Invokable() {
            @Override
            public void invoke() throws Exception {
                taskLaunchScheduledService.startAsync();
                taskLaunchScheduledService.awaitRunning();
            }
        });
        safetyInvoke("Start restful server", new Invokable() {
            @Override
            public void invoke() throws Exception {
                restfulServer.start(CloudJobRestfulApi.class.getPackage().getName());
            }
        });
    }
    
    private TreeCache getCache() {
        TreeCache result = (TreeCache) getRegCenter().getRawCache(ConfigurationNode.ROOT);
        if (null != result) {
            return result;
        }
        getRegCenter().addCacheData(ConfigurationNode.ROOT);
        return (TreeCache) getRegCenter().getRawCache(ConfigurationNode.ROOT);
    }
    
    @Override
    public synchronized void pause() {
        getCache().getListenable().removeListener(cloudJobConfigurationListener);
        safetyInvoke("Stop restful server", new Invokable() {
            @Override
            public void invoke() throws Exception {
                restfulServer.stop();
            }
        });
        safetyInvoke("Stop taskLaunchScheduledService", new Invokable() {
            @Override
            public void invoke() throws Exception {
                taskLaunchScheduledService.stopAsync();
                taskLaunchScheduledService.awaitTerminated();
            }
        });
        CloudJobRestfulApi.stop();
        facadeService.stop();
    }
    
    @Override
    public synchronized void shutdown() {
        pause();
        safetyInvoke("Stop driver", new Invokable() {
            @Override
            public void invoke() throws Exception {
                schedulerDriver.stop(true);
            }
        });
    }
    
    private void safetyInvoke(final String message, final Invokable invokable) {
        try {
            invoke(message, invokable);
            //CHECKSTYLE:OFF
        } catch (final Exception ignored) {
        }
        //CHECKSTYLE:ON
    }
    
    interface Invokable {
        void invoke() throws Exception;
    }
}
