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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos;

import com.google.common.util.concurrent.Service;
import com.netflix.fenzo.TaskScheduler;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.cloud.console.ConsoleBootstrap;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationListener;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationListener;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.MesosConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.ha.FrameworkIDService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.CloudAppDisableListener;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.CloudJobDisableListener;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics.StatisticManager;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

/**
 * Scheduler service.
 */
@Slf4j
@AllArgsConstructor
public final class SchedulerService {
    
    private static final String WEB_UI_PROTOCOL = "http://";
    
    private final BootstrapEnvironment env;
    
    private final FacadeService facadeService;
    
    private final SchedulerDriver schedulerDriver;
    
    private final ProducerManager producerManager;
    
    private final StatisticManager statisticManager;
    
    private final CloudJobConfigurationListener cloudJobConfigurationListener;
    
    private final Service taskLaunchScheduledService;
    
    private final ConsoleBootstrap consoleBootstrap;
    
    private final ReconcileService reconcileService;
    
    private final CloudJobDisableListener cloudJobDisableListener;
    
    private final CloudAppConfigurationListener cloudAppConfigurationListener;
    
    private final CloudAppDisableListener cloudAppDisableListener;
    
    public SchedulerService(final CoordinatorRegistryCenter regCenter) {
        env = BootstrapEnvironment.getINSTANCE();
        facadeService = new FacadeService(regCenter);
        statisticManager = StatisticManager.getInstance(regCenter, env.getTracingConfiguration().orElse(null));
        TaskScheduler taskScheduler = getTaskScheduler();
        JobEventBus jobEventBus = getJobEventBus();
        schedulerDriver = getSchedulerDriver(taskScheduler, jobEventBus, new FrameworkIDService(regCenter));
        producerManager = new ProducerManager(schedulerDriver, regCenter);
        cloudJobConfigurationListener = new CloudJobConfigurationListener(regCenter, producerManager);
        cloudJobDisableListener = new CloudJobDisableListener(regCenter, producerManager);
        cloudAppConfigurationListener = new CloudAppConfigurationListener(regCenter, producerManager);
        cloudAppDisableListener = new CloudAppDisableListener(regCenter, producerManager);
        taskLaunchScheduledService = new TaskLaunchScheduledService(schedulerDriver, taskScheduler, facadeService, jobEventBus);
        reconcileService = new ReconcileService(schedulerDriver, facadeService);
        consoleBootstrap = new ConsoleBootstrap(regCenter, env.getRestfulServerConfiguration(), producerManager, reconcileService);
    }
    
    private SchedulerDriver getSchedulerDriver(final TaskScheduler taskScheduler, final JobEventBus jobEventBus, final FrameworkIDService frameworkIDService) {
        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder();
        frameworkIDService.fetch().ifPresent(frameworkID -> builder.setId(Protos.FrameworkID.newBuilder().setValue(frameworkID).build()));
        Optional<String> role = env.getMesosRole();
        String frameworkName = MesosConfiguration.FRAMEWORK_NAME;
        if (role.isPresent()) {
            builder.setRole(role.get());
            frameworkName += "-" + role.get();
        }
        builder.addCapabilitiesBuilder().setType(Protos.FrameworkInfo.Capability.Type.PARTITION_AWARE);
        MesosConfiguration mesosConfig = env.getMesosConfiguration();
        Protos.FrameworkInfo frameworkInfo = builder.setUser(mesosConfig.getUser()).setName(frameworkName)
                .setHostname(mesosConfig.getHostname()).setFailoverTimeout(MesosConfiguration.FRAMEWORK_FAILOVER_TIMEOUT_SECONDS)
                .setWebuiUrl(WEB_UI_PROTOCOL + env.getFrameworkHostPort()).setCheckpoint(true).build();
        return new MesosSchedulerDriver(new SchedulerEngine(taskScheduler, facadeService, jobEventBus, frameworkIDService, statisticManager), frameworkInfo, mesosConfig.getUrl());
    }
    
    private TaskScheduler getTaskScheduler() {
        return new TaskScheduler.Builder()
                .withLeaseOfferExpirySecs(1000000000L)
                .withLeaseRejectAction(lease -> {
                    log.warn("Declining offer on '{}'", lease.hostname());
                    schedulerDriver.declineOffer(lease.getOffer().getId());
                }).build();
    }
    
    private JobEventBus getJobEventBus() {
        Optional<TracingConfiguration> tracingConfiguration = env.getTracingConfiguration();
        return tracingConfiguration.map(JobEventBus::new).orElseGet(JobEventBus::new);
    }
    
    /**
     * Start as a daemon.
     */
    public void start() {
        facadeService.start();
        producerManager.startup();
        statisticManager.startup();
        cloudJobConfigurationListener.start();
        cloudAppConfigurationListener.start();
        cloudJobDisableListener.start();
        cloudAppDisableListener.start();
        taskLaunchScheduledService.startAsync();
        consoleBootstrap.start();
        schedulerDriver.start();
        if (env.getFrameworkConfiguration().isEnabledReconcile()) {
            reconcileService.startAsync();
        }
    }
    
    /**
     * Stop.
     */
    public void stop() {
        consoleBootstrap.stop();
        taskLaunchScheduledService.stopAsync();
        cloudJobConfigurationListener.stop();
        cloudAppConfigurationListener.stop();
        cloudJobDisableListener.stop();
        cloudAppDisableListener.stop();
        statisticManager.shutdown();
        producerManager.shutdown();
        schedulerDriver.stop(true);
        facadeService.stop();
        if (env.getFrameworkConfiguration().isEnabledReconcile()) {
            reconcileService.stopAsync();
        }
    }
}
