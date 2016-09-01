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

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.config.JobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.config.ConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.Collection;

/**
 * 发布任务作业调度注册表.
 *
 * @author caohao
 */
public class TaskProducerSchedulerRegistry {
    
    private static volatile TaskProducerSchedulerRegistry instance;
    
    private final TaskProducerScheduler schedulerInstance;
    
    private final ConfigurationService configService;
    
    private final ReadyService readyService;
    
    private TaskProducerSchedulerRegistry(final CoordinatorRegistryCenter regCenter) {
        configService = new ConfigurationService(regCenter);
        schedulerInstance = new TaskProducerScheduler(regCenter);
        readyService = new ReadyService(regCenter);
    }
    
    /**
     * 获取实例.
     * 
     * @param regCenter 注册中心对象
     * @return 实例对象
     */
    public static TaskProducerSchedulerRegistry getInstance(final CoordinatorRegistryCenter regCenter) {
        if (null == instance) {
            synchronized (TaskProducerSchedulerRegistry.class) {
                if (null == instance) {
                    instance = new TaskProducerSchedulerRegistry(regCenter);
                }
            }
        }
        return instance;
    }
    
    /**
     * 启动作业调度器.
     */
    public void startup() {
        Collection<CloudJobConfiguration> configs = configService.loadAll();
        schedulerInstance.startup(filterJobConfiguration(configs, JobExecutionType.TRANSIENT));
        for (CloudJobConfiguration each : filterJobConfiguration(configs, JobExecutionType.DAEMON)) {
            readyService.addDaemon(each.getJobName());
        }
    }
    
    private Collection<CloudJobConfiguration> filterJobConfiguration(final Collection<CloudJobConfiguration> configs, final JobExecutionType jobExecutionType) {
        return Collections2.filter(configs, new Predicate<CloudJobConfiguration>() {
            
            @Override
            public boolean apply(final CloudJobConfiguration input) {
                return jobExecutionType == input.getJobExecutionType();
            }
        });
    }
    
    /**
     * 注册作业.
     * 
     * @param jobConfig 作业配置
     */
    public void register(final CloudJobConfiguration jobConfig) {
        if (JobExecutionType.TRANSIENT == jobConfig.getJobExecutionType()) {
            schedulerInstance.register(jobConfig);
        } else if (JobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
            readyService.addDaemon(jobConfig.getJobName()); 
        }
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobConfig.getJobName());
        if (!jobConfigFromZk.isPresent()) {
            configService.add(jobConfig);
        } else if (!jobConfigFromZk.get().equals(jobConfig)) {
            configService.update(jobConfig);
        }
    }
    
    /**
     * 注销作业.
     * 
     * @param jobName 作业名称
     */
    public void deregister(final String jobName) {
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobName);
        if (jobConfigFromZk != null && jobConfigFromZk.isPresent()) {
            schedulerInstance.deregister(jobConfigFromZk.get());
            configService.remove(jobName);
        }
    }
    
    /**
     * 关闭作业调度器.
     */
    public void shutdown() {
        schedulerInstance.shutdown();
    }
}
