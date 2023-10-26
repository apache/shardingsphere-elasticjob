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

package org.apache.shardingsphere.elasticjob.executor.threadpool;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;

/**
 * Executor service reloader.
 */
public final class ExecutorServiceReloader implements Closeable {
    
    private String jobExecutorThreadPoolSizeProviderType;
    
    @Getter
    private ExecutorService executorService;
    
    public ExecutorServiceReloader(final JobConfiguration jobConfig) {
        JobExecutorThreadPoolSizeProvider jobExecutorThreadPoolSizeProvider = TypedSPILoader.getService(JobExecutorThreadPoolSizeProvider.class, jobConfig.getJobExecutorThreadPoolSizeProviderType());
        jobExecutorThreadPoolSizeProviderType = jobExecutorThreadPoolSizeProvider.getType();
        executorService = new ElasticJobExecutorService("elasticjob-" + jobConfig.getJobName(), jobExecutorThreadPoolSizeProvider.getSize()).createExecutorService();
    }
    
    /**
     * Reload if necessary.
     *
     * @param jobConfig job configuration
     */
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        if (jobExecutorThreadPoolSizeProviderType.equals(jobConfig.getJobExecutorThreadPoolSizeProviderType())) {
            return;
        }
        reload(jobConfig.getJobExecutorThreadPoolSizeProviderType(), jobConfig.getJobName());
        jobExecutorThreadPoolSizeProviderType = jobConfig.getJobExecutorThreadPoolSizeProviderType();
    }
    
    private void reload(final String jobExecutorThreadPoolSizeProviderType, final String jobName) {
        executorService.shutdown();
        executorService = new ElasticJobExecutorService(
                "elasticjob-" + jobName, TypedSPILoader.getService(JobExecutorThreadPoolSizeProvider.class, jobExecutorThreadPoolSizeProviderType).getSize()).createExecutorService();
    }
    
    @Override
    public void close() {
        executorService.shutdown();
    }
}
