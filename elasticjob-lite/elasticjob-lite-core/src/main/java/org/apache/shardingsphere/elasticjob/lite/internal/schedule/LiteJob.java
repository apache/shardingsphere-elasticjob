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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import lombok.Setter;
import org.apache.shardingsphere.elasticjob.lite.api.job.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.job.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.List;

/**
 * Lite job class.
 */
@Setter
public final class LiteJob implements Job {
    
    private CoordinatorRegistryCenter regCenter;
    
    private ElasticJob elasticJob;
    
    private String elasticJobType;
    
    private JobConfiguration jobConfig;
    
    private List<ElasticJobListener> elasticJobListeners;
    
    private TracingConfiguration tracingConfig;
    
    @Override
    public void execute(final JobExecutionContext context) {
        createExecutor().execute();
    }
    
    private ElasticJobExecutor createExecutor() {
        return null == elasticJob
                ? new ElasticJobExecutor(regCenter, elasticJobType, jobConfig, elasticJobListeners, tracingConfig)
                : new ElasticJobExecutor(regCenter, elasticJob, jobConfig, elasticJobListeners, tracingConfig);
    }
}
