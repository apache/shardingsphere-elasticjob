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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.producer;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.SchedulerDriver;
import org.apache.shardingsphere.elasticjob.infra.context.TaskContext;
import org.apache.shardingsphere.elasticjob.cloud.exception.AppConfigurationException;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.app.CloudAppConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job.CloudJobConfigurationService;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app.DisableAppService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.job.DisableJobService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.running.RunningService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Optional;

/**
 * Producer manager.
 */
@Slf4j
public final class ProducerManager {
    
    private final CloudAppConfigurationService appConfigService;
    
    private final CloudJobConfigurationService configService;
            
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final DisableAppService disableAppService;
    
    private final DisableJobService disableJobService;
    
    private final TransientProducerScheduler transientProducerScheduler;
    
    private final SchedulerDriver schedulerDriver;
    
    public ProducerManager(final SchedulerDriver schedulerDriver, final CoordinatorRegistryCenter regCenter) {
        this.schedulerDriver = schedulerDriver;
        appConfigService = new CloudAppConfigurationService(regCenter);
        configService = new CloudJobConfigurationService(regCenter);
        readyService = new ReadyService(regCenter);
        runningService = new RunningService(regCenter);
        disableAppService = new DisableAppService(regCenter);
        disableJobService = new DisableJobService(regCenter);
        transientProducerScheduler = new TransientProducerScheduler(readyService);
    }
    
    /**
     * Start the producer manager.
     */
    public void startup() {
        log.info("Start producer manager");
        transientProducerScheduler.start();
        for (CloudJobConfiguration each : configService.loadAll()) {
            schedule(each);
        }
    }
    
    /**
     * Register the job.
     * 
     * @param cloudJobConfig cloud job configuration
     */
    public void register(final CloudJobConfiguration cloudJobConfig) {
        if (disableJobService.isDisabled(cloudJobConfig.getJobConfig().getJobName())) {
            throw new JobConfigurationException("Job '%s' has been disable.", cloudJobConfig.getJobConfig().getJobName());
        }
        Optional<CloudAppConfiguration> appConfigFromZk = appConfigService.load(cloudJobConfig.getAppName());
        if (!appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("Register app '%s' firstly.", cloudJobConfig.getAppName());
        }
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(cloudJobConfig.getJobConfig().getJobName());
        if (jobConfigFromZk.isPresent()) {
            throw new JobConfigurationException("Job '%s' already existed.", cloudJobConfig.getJobConfig().getJobName());
        }
        configService.add(cloudJobConfig);
        schedule(cloudJobConfig);
    }
    
    /**
     * Update the job.
     *
     * @param cloudJobConfig cloud job configuration
     */
    public void update(final CloudJobConfiguration cloudJobConfig) {
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(cloudJobConfig.getJobConfig().getJobName());
        if (!jobConfigFromZk.isPresent()) {
            throw new JobConfigurationException("Cannot found job '%s', please register first.", cloudJobConfig.getJobConfig().getJobName());
        }
        configService.update(cloudJobConfig);
        reschedule(cloudJobConfig.getJobConfig().getJobName());
    }
    
    /**
     * Deregister the job.
     * 
     * @param jobName job name
     */
    public void deregister(final String jobName) {
        Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
        if (jobConfig.isPresent()) {
            disableJobService.remove(jobName);
            configService.remove(jobName);
        }
        unschedule(jobName);
    }
    
    /**
     * Schedule the job.
     *
     * @param cloudJobConfig cloud job configuration
     */
    public void schedule(final CloudJobConfiguration cloudJobConfig) {
        if (disableAppService.isDisabled(cloudJobConfig.getAppName()) || disableJobService.isDisabled(cloudJobConfig.getJobConfig().getJobName())) {
            return;
        }
        if (CloudJobExecutionType.TRANSIENT == cloudJobConfig.getJobExecutionType()) {
            transientProducerScheduler.register(cloudJobConfig);
        } else if (CloudJobExecutionType.DAEMON == cloudJobConfig.getJobExecutionType()) {
            readyService.addDaemon(cloudJobConfig.getJobConfig().getJobName());
        }
    }
    
    /**
     * Stop to schedule the job.
     *
     * @param jobName job name
     */
    public void unschedule(final String jobName) {
        for (TaskContext each : runningService.getRunningTasks(jobName)) {
            schedulerDriver.killTask(Protos.TaskID.newBuilder().setValue(each.getId()).build());
        }
        runningService.remove(jobName);
        readyService.remove(Lists.newArrayList(jobName));
        Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
        jobConfig.ifPresent(transientProducerScheduler::deregister);
    }
    
    /**
     * Re-schedule the job.
     *
     * @param jobName job name
     */
    public void reschedule(final String jobName) {
        unschedule(jobName);
        Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
        jobConfig.ifPresent(this::schedule);
    }
    
    /**
     * Send message to executor.
     * 
     * @param executorId the executor of which to receive message
     * @param slaveId the slave id of the executor
     * @param data message content
     */
    public void sendFrameworkMessage(final ExecutorID executorId, final SlaveID slaveId, final byte[] data) {
        schedulerDriver.sendFrameworkMessage(executorId, slaveId, data);
    }
    
    /**
     * Shutdown the producer manager.
     */
    public void shutdown() {
        log.info("Stop producer manager");
        transientProducerScheduler.shutdown();
    }
}
