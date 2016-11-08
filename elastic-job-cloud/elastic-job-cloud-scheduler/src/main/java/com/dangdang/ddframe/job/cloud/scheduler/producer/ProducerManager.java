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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import com.dangdang.ddframe.job.cloud.scheduler.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.lifecycle.LifecycleService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.mesos.SchedulerDriver;

/**
 * 发布任务作业调度管理器.
 *
 * @author caohao
 * @author zhangliang
 */
public class ProducerManager {
    
    private final ConfigurationService configService;
            
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final TransientProducerScheduler transientProducerScheduler;
    
    private final LifecycleService lifecycleService;
    
    ProducerManager(final SchedulerDriver schedulerDriver, final CoordinatorRegistryCenter regCenter) {
        configService = new ConfigurationService(regCenter);
        readyService = new ReadyService(regCenter);
        runningService = new RunningService();
        transientProducerScheduler = new TransientProducerScheduler(readyService);
        lifecycleService = new LifecycleService(schedulerDriver);
    }
    
    /**
     * 启动作业调度器.
     */
    public void startup() {
        for (CloudJobConfiguration each : configService.loadAll()) {
            schedule(each);
        }
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    public void register(final CloudJobConfiguration jobConfig) {
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobConfig.getJobName());
        if (jobConfigFromZk.isPresent()) {
            throw new JobConfigurationException("job '%s' already existed.", jobConfig.getJobName());
        }
        configService.add(jobConfig);
        schedule(jobConfig);
    }
    
    /**
     * 更新作业配置.
     *
     * @param jobConfig 作业配置
     */
    public void update(final CloudJobConfiguration jobConfig) {
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobConfig.getJobName());
        if (!jobConfigFromZk.isPresent()) {
            throw new JobConfigurationException("Cannot found job '%s', please register first.", jobConfig.getJobName());
        }
        configService.update(jobConfig);
        reschedule(jobConfig);
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    public void deregister(final String jobName) {
        Optional<CloudJobConfiguration> jobConfig = configService.load(jobName);
        if (jobConfig.isPresent()) {
            configService.remove(jobName);
            transientProducerScheduler.deregister(jobConfig.get());
        }
        unschedule(jobName);
    }
    
    /**
     * 调度作业.
     * 
     * @param jobConfig 作业配置
     */
    public void schedule(final CloudJobConfiguration jobConfig) {
        if (JobExecutionType.TRANSIENT == jobConfig.getJobExecutionType()) {
            transientProducerScheduler.register(jobConfig);
        } else if (JobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
            readyService.addDaemon(jobConfig.getJobName());
        }
    }
    
    /**
     * 停止调度作业.
     *
     * @param jobName 作业名称
     */
    public void unschedule(final String jobName) {
        lifecycleService.killJob(jobName);
        runningService.remove(jobName);
        readyService.remove(Lists.newArrayList(jobName));
    }
    
    /**
     * 重新调度作业.
     *
     * @param jobConfig 作业配置
     */
    public void reschedule(final CloudJobConfiguration jobConfig) {
        unschedule(jobConfig.getJobName());
        schedule(jobConfig);
    }
    
    /**
     * 关闭作业调度器.
     */
    public void shutdown() {
        transientProducerScheduler.shutdown();
    }
}
