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

package io.elasticjob.lite.spring.api;

import org.springframework.beans.factory.DisposableBean;

import com.google.common.base.Optional;
import io.elasticjob.lite.api.ElasticJob;
import io.elasticjob.lite.api.JobScheduler;
import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.event.JobEventConfiguration;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.lite.spring.job.util.AopTargetUtils;

/**
 * 基于Spring的作业启动器.
 */
public final class SpringJobScheduler extends JobScheduler implements DisposableBean {
    
    private final ElasticJob elasticJob;
    
    public SpringJobScheduler(final ElasticJob elasticJob, final CoordinatorRegistryCenter regCenter, final LiteJobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        super(regCenter, jobConfig, getTargetElasticJobListeners(elasticJobListeners));
        this.elasticJob = elasticJob;
    }
    
    public SpringJobScheduler(final ElasticJob elasticJob, final CoordinatorRegistryCenter regCenter, final LiteJobConfiguration jobConfig,
                              final JobEventConfiguration jobEventConfig, final ElasticJobListener... elasticJobListeners) {
        super(regCenter, jobConfig, jobEventConfig, getTargetElasticJobListeners(elasticJobListeners));
        this.elasticJob = elasticJob;
    }
    
    private static ElasticJobListener[] getTargetElasticJobListeners(final ElasticJobListener[] elasticJobListeners) {
        final ElasticJobListener[] result = new ElasticJobListener[elasticJobListeners.length];
        for (int i = 0; i < elasticJobListeners.length; i++) {
            result[i] = (ElasticJobListener) AopTargetUtils.getTarget(elasticJobListeners[i]);
        }
        return result;
    }
    
    @Override
    protected Optional<ElasticJob> createElasticJobInstance() {
        return Optional.fromNullable(elasticJob);
    }
    
    @Override
    public void destroy() {
        shutdown();
    }
}
