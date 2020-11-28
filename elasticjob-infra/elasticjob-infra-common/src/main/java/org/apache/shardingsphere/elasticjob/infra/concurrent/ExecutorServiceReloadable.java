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

package org.apache.shardingsphere.elasticjob.infra.concurrent;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.Reloadable;
import org.apache.shardingsphere.elasticjob.infra.context.ReloadablePostProcessor;
import org.apache.shardingsphere.elasticjob.infra.handler.threadpool.JobExecutorServiceHandlerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Executor service reloadable.
 */
@Slf4j
public final class ExecutorServiceReloadable implements Reloadable<ExecutorService>, ReloadablePostProcessor {
    
    private String jobExecutorServiceHandlerType;
    
    private ExecutorService executorService;
    
    @Override
    public void init(final JobConfiguration jobConfig) {
        jobExecutorServiceHandlerType = Strings.isNullOrEmpty(jobConfig.getJobExecutorServiceHandlerType())
                ? JobExecutorServiceHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobExecutorServiceHandlerType();
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobExecutorServiceHandlerType).createExecutorService(jobConfig.getJobName());
    }
    
    @Override
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        String newJobExecutorServiceHandlerType = Strings.isNullOrEmpty(jobConfig.getJobExecutorServiceHandlerType())
                ? JobExecutorServiceHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobExecutorServiceHandlerType();
        if (newJobExecutorServiceHandlerType.equals(jobExecutorServiceHandlerType)) {
            return;
        }
        log.debug("JobExecutorServiceHandler reload occurred in the job '{}'. Change from '{}' to '{}'.", jobConfig.getJobName(), jobExecutorServiceHandlerType, newJobExecutorServiceHandlerType);
        reload(newJobExecutorServiceHandlerType, jobConfig.getJobName());
    }
    
    private void reload(final String jobExecutorServiceHandlerType, final String jobName) {
        executorService.shutdown();
        this.jobExecutorServiceHandlerType = jobExecutorServiceHandlerType;
        executorService = JobExecutorServiceHandlerFactory.getHandler(jobExecutorServiceHandlerType).createExecutorService(jobName);
    }
    
    @Override
    public ExecutorService getInstance() {
        return executorService;
    }
    
    @Override
    public void close() {
        Optional.ofNullable(executorService).ifPresent(ExecutorService::shutdown);
    }
    
    @Override
    public String getType() {
        return ExecutorService.class.getName();
    }
}
