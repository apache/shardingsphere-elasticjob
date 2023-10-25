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

package org.apache.shardingsphere.elasticjob.infra.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.Reloadable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Executor service reloadable.
 */
@Slf4j
public final class ExecutorServiceReloadable implements Reloadable<ExecutorService> {
    
    private String jobExecutorThreadPoolSizeProviderType;
    
    private ExecutorService executorService;
    
    @Override
    public void init(final JobConfiguration jobConfig) {
        JobExecutorThreadPoolSizeProvider jobExecutorThreadPoolSizeProvider = TypedSPILoader.getService(JobExecutorThreadPoolSizeProvider.class, jobConfig.getJobExecutorThreadPoolSizeProviderType());
        jobExecutorThreadPoolSizeProviderType = jobExecutorThreadPoolSizeProvider.getType();
        executorService = new ElasticJobExecutorService("elasticjob-" + jobConfig.getJobName(), jobExecutorThreadPoolSizeProvider.getSize()).createExecutorService();
    }
    
    @Override
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        if (jobExecutorThreadPoolSizeProviderType.equals(jobConfig.getJobExecutorThreadPoolSizeProviderType())) {
            return;
        }
        log.debug("Reload occurred in the job '{}'. Change from '{}' to '{}'.", jobConfig.getJobName(), jobExecutorThreadPoolSizeProviderType, jobConfig.getJobExecutorThreadPoolSizeProviderType());
        reload(jobConfig.getJobExecutorThreadPoolSizeProviderType(), jobConfig.getJobName());
    }
    
    private void reload(final String jobExecutorThreadPoolSizeProviderType, final String jobName) {
        executorService.shutdown();
        executorService = new ElasticJobExecutorService(
                "elasticjob-" + jobName, TypedSPILoader.getService(JobExecutorThreadPoolSizeProvider.class, jobExecutorThreadPoolSizeProviderType).getSize()).createExecutorService();
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
    public Class<ExecutorService> getType() {
        return ExecutorService.class;
    }
}
