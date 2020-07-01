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

package org.apache.shardingsphere.elasticjob.lite.api.bootstrap;

import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;

/**
 * One off job bootstrap.
 */
public final class OneOffJobBootstrap extends JobBootstrap {
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        super(regCenter, elasticJob, jobConfig, elasticJobListeners);
    }
    
    public OneOffJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                              final ElasticJobListener... elasticJobListeners) {
        super(regCenter, elasticJob, jobConfig, tracingConfig, elasticJobListeners);
        registerStartUpInfo();
    }
    
    /**
     * Execute job.
     */
    public void execute() {
        new ElasticJobExecutor(getRegCenter(), getElasticJob(), getJobConfig(), getElasticJobListeners(), getTracingConfig()).execute();
    }
}
