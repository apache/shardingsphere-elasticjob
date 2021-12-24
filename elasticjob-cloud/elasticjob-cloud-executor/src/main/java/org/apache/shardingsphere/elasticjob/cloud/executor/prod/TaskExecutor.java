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

package org.apache.shardingsphere.elasticjob.cloud.executor.prod;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.facade.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.infra.concurrent.ElasticJobExecutorService;
import org.apache.shardingsphere.elasticjob.infra.exception.ExceptionUtils;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Task executor.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class TaskExecutor implements Executor {
    
    private final ElasticJob elasticJob;
    
    private final String elasticJobType;
    
    private final ExecutorService executorService = new ElasticJobExecutorService("cloud-task-executor", Runtime.getRuntime().availableProcessors() * 100).createExecutorService();
    
    private volatile ElasticJobExecutor jobExecutor;
    
    private volatile JobTracingEventBus jobTracingEventBus = new JobTracingEventBus();
    
    public TaskExecutor(final ElasticJob elasticJob) {
        this(elasticJob, null);
    }
    
    public TaskExecutor(final String elasticJobType) {
        this(null, elasticJobType);
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
            jobTracingEventBus = new JobTracingEventBus(new TracingConfiguration<DataSource>("RDB", dataSource));
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
        if (null != bytes && "STOP".equals(new String(bytes))) {
            log.error("call frameworkMessage executor stopped.");
            executorDriver.stop();
        }
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
            Thread.currentThread().setContextClassLoader(TaskThread.class.getClassLoader());
            executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_RUNNING).build());
            Map<String, Object> data = SerializationUtils.deserialize(taskInfo.getData().toByteArray());
            ShardingContexts shardingContexts = (ShardingContexts) data.get("shardingContext");
            JobConfiguration jobConfig = YamlEngine.unmarshal(data.get("jobConfigContext").toString(), JobConfigurationPOJO.class).toJobConfiguration();
            try {
                JobFacade jobFacade = new CloudJobFacade(shardingContexts, jobConfig, jobTracingEventBus);
                if (isTransient(jobConfig)) {
                    getJobExecutor(jobFacade).execute();
                    executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_FINISHED).build());
                } else {
                    new DaemonTaskScheduler(elasticJob, elasticJobType, jobConfig, jobFacade, executorDriver, taskInfo.getTaskId()).init();
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("ElasticJob-Cloud Executor error:", ex);
                executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId()).setState(Protos.TaskState.TASK_ERROR).setMessage(ExceptionUtils.transform(ex)).build());
                executorDriver.stop();
                throw ex;
            }
        }
    
        private boolean isTransient(final JobConfiguration jobConfig) {
            return Strings.isNullOrEmpty(jobConfig.getCron());
        }
        
        private ElasticJobExecutor getJobExecutor(final JobFacade jobFacade) {
            if (null == jobExecutor) {
                createJobExecutor(jobFacade);
            }
            return jobExecutor;
        }
    
        private synchronized void createJobExecutor(final JobFacade jobFacade) {
            if (null != jobExecutor) {
                return;
            }
            jobExecutor = null == elasticJob
                    ? new ElasticJobExecutor(elasticJobType, jobFacade.loadJobConfiguration(true), jobFacade)
                    : new ElasticJobExecutor(elasticJob, jobFacade.loadJobConfiguration(true), jobFacade);
        }
    }
}
