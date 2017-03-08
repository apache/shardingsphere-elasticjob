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

import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.config.job.CloudJobExecutionType;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfiguration;
import com.dangdang.ddframe.job.cloud.scheduler.config.app.CloudAppConfigurationService;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import com.dangdang.ddframe.job.cloud.scheduler.state.running.RunningService;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.exception.AppConfigurationException;
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * 发布任务作业调度管理器.
 *
 * @author caohao
 * @author zhangliang
 */
@Slf4j
public class ProducerManager {
    
    private final CloudAppConfigurationService appConfigService;
    
    private final CloudJobConfigurationService configService;
            
    private final ReadyService readyService;
    
    private final RunningService runningService;
    
    private final TransientProducerScheduler transientProducerScheduler;
    
    private final SchedulerDriver schedulerDriver;
    
    public ProducerManager(final SchedulerDriver schedulerDriver, final CoordinatorRegistryCenter regCenter) {
        this.schedulerDriver = schedulerDriver;
        appConfigService = new CloudAppConfigurationService(regCenter);
        configService = new CloudJobConfigurationService(regCenter);
        readyService = new ReadyService(regCenter);
        runningService = new RunningService(regCenter);
        transientProducerScheduler = new TransientProducerScheduler(readyService);
    }
    
    /**
     * 启动作业调度器.
     */
    public void startup() {
        log.info("Start producer manager");
        transientProducerScheduler.start();
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
        Optional<CloudAppConfiguration> appConfigFromZk = appConfigService.load(jobConfig.getAppName());
        if (!appConfigFromZk.isPresent()) {
            throw new AppConfigurationException("Register app '%s' firstly.", jobConfig.getAppName());
        }
        Optional<CloudJobConfiguration> jobConfigFromZk = configService.load(jobConfig.getJobName());
        if (jobConfigFromZk.isPresent()) {
            throw new JobConfigurationException("Job '%s' already existed.", jobConfig.getJobName());
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
        if (CloudJobExecutionType.TRANSIENT == jobConfig.getJobExecutionType()) {
            transientProducerScheduler.register(jobConfig);
        } else if (CloudJobExecutionType.DAEMON == jobConfig.getJobExecutionType()) {
            readyService.addDaemon(jobConfig.getJobName());
        }
    }
    
    /**
     * 停止调度作业.
     *
     * @param jobName 作业名称
     */
    public void unschedule(final String jobName) {
        for (TaskContext each : runningService.getRunningTasks(jobName)) {
            schedulerDriver.killTask(Protos.TaskID.newBuilder().setValue(each.getId()).build());
        }
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
        log.info("Stop producer manager");
        transientProducerScheduler.shutdown();
    }
    
    // TODO 挪到ReconcileService中
    /**
     * 为特定的任务进行显式协调.
     * 
     * @param taskId 任务ID
     */
    public void explicitReconcile(final String taskId) {
        explicitReconcile(Collections.singletonList(Iterators.find(runningService.getRunningTasks(TaskContext.from(taskId).getMetaInfo().getJobName()).iterator(), new Predicate<TaskContext>() {
            @Override
            public boolean apply(final TaskContext input) {
                return input.getId().equals(taskId);
            }
        })));
    }
    
    private void explicitReconcile(final Collection<TaskContext> taskContexts) {
        schedulerDriver.reconcileTasks(Collections2.transform(taskContexts, new Function<TaskContext, Protos.TaskStatus>() {
            @Override
            public Protos.TaskStatus apply(final TaskContext input) {
                return Protos.TaskStatus.newBuilder()
                        .setTaskId(Protos.TaskID.newBuilder().setValue(input.getId()).build())
                        .setSlaveId(Protos.SlaveID.newBuilder().setValue(input.getSlaveId()).build())
                        .setState(Protos.TaskState.TASK_RUNNING).build();
            }
        }));
    }
    
    // TODO 挪到ReconcileService中
    /**
     * 全量的显示协调.
     */
    public void explicitReconcile() {
        Set<TaskContext> runningDaemonTask = runningService.getAllRunningDaemonTasks();
        if (!runningDaemonTask.isEmpty()) {
            explicitReconcile(runningDaemonTask);
        }
    }
    
    // TODO 挪到ReconcileService中
    /**
     * 隐式协调.
     */
    public void implicitReconcile() {
        schedulerDriver.reconcileTasks(Collections.<Protos.TaskStatus>emptyList());
    }
}
