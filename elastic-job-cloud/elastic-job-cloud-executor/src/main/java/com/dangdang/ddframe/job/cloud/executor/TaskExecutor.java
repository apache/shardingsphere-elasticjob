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

package com.dangdang.ddframe.job.cloud.executor;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.JobExecutorFactory;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.util.concurrent.ExecutorServiceObject;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskInfo;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 作业任务执行器.
 *
 * @author zhangliang
 */
@Slf4j
public final class TaskExecutor implements Executor {
    
    private final ExecutorService executorService;
    
    private final Map<String, ClassPathXmlApplicationContext> applicationContexts = new HashMap<>();
    
    private volatile JobEventBus jobEventBus = new JobEventBus();
    
    public TaskExecutor() {
        executorService = new ExecutorServiceObject("cloud-task-executor", Runtime.getRuntime().availableProcessors() * 100).createExecutorService();
    }
    
    @Override
    public void registered(final ExecutorDriver executorDriver, final Protos.ExecutorInfo executorInfo, final Protos.FrameworkInfo frameworkInfo, final Protos.SlaveInfo slaveInfo) {
        if (!executorInfo.getData().isEmpty()) {
            Map<String, String> data = SerializationUtils.deserialize(executorInfo.getData().toByteArray());
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(data.get("event_trace_rdb_driver"));
            dataSource.setUrl(data.get("event_trace_rdb_url"));
            dataSource.setPassword(data.get("event_trace_rdb_password"));
            dataSource.setUsername(data.get("event_trace_rdb_username"));
            jobEventBus = new JobEventBus(new JobEventRdbConfiguration(dataSource));
        }
    }
    
    @Override
    public void reregistered(final ExecutorDriver executorDriver, final Protos.SlaveInfo slaveInfo) {
    }
    
    @Override
    public void disconnected(final ExecutorDriver executorDriver) {
    }
    
    @Override
    public void launchTask(final ExecutorDriver executorDriver, final Protos.TaskInfo taskInfo) {
        executorService.submit(new TaskThread(executorDriver, taskInfo));
    }
    
    @Override
    public void killTask(final ExecutorDriver executorDriver, final Protos.TaskID taskID) {
        executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskID).setState(Protos.TaskState.TASK_KILLED).build());
        DaemonTaskScheduler.shutdown(taskID);
    }
    
    @Override
    public void frameworkMessage(final ExecutorDriver executorDriver, final byte[] bytes) {
    }
    
    @Override
    public void shutdown(final ExecutorDriver executorDriver) {
    }
    
    @Override
    public void error(final ExecutorDriver executorDriver, final String message) {
        log.error("call executor error, message is: {}", message);
    }
    
    @RequiredArgsConstructor
    class TaskThread implements Runnable {
        
        private final ExecutorDriver executorDriver;
        
        private final TaskInfo taskInfo;
        
        @Override
        public void run() {
            executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_RUNNING).build());
            Map<String, Object> data = SerializationUtils.deserialize(taskInfo.getData().toByteArray());
            ShardingContexts shardingContexts = (ShardingContexts) data.get("shardingContext");
            @SuppressWarnings("unchecked")
            JobConfigurationContext jobConfig = new JobConfigurationContext((Map<String, String>) data.get("jobConfigContext"));
            try {
                ElasticJob elasticJob = getElasticJobInstance(jobConfig);
                final CloudJobFacade jobFacade = new CloudJobFacade(shardingContexts, jobConfig, jobEventBus);
                if (jobConfig.isTransient()) {
                    JobExecutorFactory.getJobExecutor(elasticJob, jobFacade).execute();
                    executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_FINISHED).build());
                } else {
                    new DaemonTaskScheduler(elasticJob, jobConfig, jobFacade, executorDriver, taskInfo.getTaskId()).init();
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_ERROR).build());
                executorDriver.stop();
                throw ex;
            }
        }
        
        private ElasticJob getElasticJobInstance(final JobConfigurationContext jobConfig) {
            if (!Strings.isNullOrEmpty(jobConfig.getBeanName()) && !Strings.isNullOrEmpty(jobConfig.getApplicationContext())) {
                return getElasticJobBean(jobConfig);
            } else {
                return getElasticJobClass(jobConfig);
            }
        }
        
        private ElasticJob getElasticJobBean(final JobConfigurationContext jobConfig) {
            String applicationContextFile = jobConfig.getApplicationContext();
            if (null == applicationContexts.get(applicationContextFile)) {
                synchronized (applicationContexts) {
                    if (null == applicationContexts.get(applicationContextFile)) {
                        applicationContexts.put(applicationContextFile, new ClassPathXmlApplicationContext(applicationContextFile));
                    }
                }
            }
            return (ElasticJob) applicationContexts.get(applicationContextFile).getBean(jobConfig.getBeanName());
        }
        
        private ElasticJob getElasticJobClass(final JobConfigurationContext jobConfig) {
            String jobClass = jobConfig.getTypeConfig().getJobClass();
            try {
                Class<?> elasticJobClass = Class.forName(jobClass);
                if (!ElasticJob.class.isAssignableFrom(elasticJobClass)) {
                    throw new JobSystemException("Elastic-Job: Class '%s' must implements ElasticJob interface.", jobClass);
                }
                if (elasticJobClass != ScriptJob.class) {
                    return (ElasticJob) elasticJobClass.newInstance();
                }
                return null;
            } catch (final ReflectiveOperationException ex) {
                throw new JobSystemException("Elastic-Job: Class '%s' initialize failure, the error message is '%s'.", jobClass, ex.getMessage());
            }
        }
    }
}
