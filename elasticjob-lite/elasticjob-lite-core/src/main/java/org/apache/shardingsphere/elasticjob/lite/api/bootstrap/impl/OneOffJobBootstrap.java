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

package org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl;

import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduler;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;

/**
 * One off job bootstrap.
 */
public final class OneOffJobBootstrap implements JobBootstrap {
    
    private final JobScheduler jobScheduler;
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        jobScheduler = new JobScheduler(regCenter, elasticJob, jobConfig, elasticJobListeners);
    }
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                              final ElasticJobListener... elasticJobListeners) {
        jobScheduler = new JobScheduler(regCenter, elasticJob, jobConfig, tracingConfig, elasticJobListeners);
    }
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        jobScheduler = new JobScheduler(regCenter, elasticJobType, jobConfig, elasticJobListeners);
    }
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                              final ElasticJobListener... elasticJobListeners) {
        jobScheduler = new JobScheduler(regCenter, elasticJobType, jobConfig, tracingConfig, elasticJobListeners);
    }
    
    /**
     * Execute job.
     */
    public void execute() {
        jobScheduler.getJobScheduleController().executeJob();
    }
    
    @Override
    public void shutdown() {
        jobScheduler.shutdown();
    }
}
