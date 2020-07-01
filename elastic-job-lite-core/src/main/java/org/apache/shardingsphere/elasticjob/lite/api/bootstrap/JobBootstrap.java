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

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.script.ScriptJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.guarantee.GuaranteeService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
import org.apache.shardingsphere.elasticjob.lite.internal.setup.SetUpFacade;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Schedule job bootstrap.
 */
@Getter
public abstract class JobBootstrap {
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ElasticJob elasticJob;
    
    private final JobConfiguration jobConfig;
    
    private final List<ElasticJobListener> elasticJobListeners;
    
    private final TracingConfiguration tracingConfig;
    
    private final SetUpFacade setUpFacade;
    
    private final SchedulerFacade schedulerFacade;
    
    public JobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        this(regCenter, elasticJob, jobConfig, null, elasticJobListeners);
    }
    
    public JobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig, final TracingConfiguration tracingConfig,
                        final ElasticJobListener... elasticJobListeners) {
        this.regCenter = regCenter;
        this.elasticJob = elasticJob;
        this.elasticJobListeners = Arrays.asList(elasticJobListeners);
        this.tracingConfig = tracingConfig;
        setUpFacade = new SetUpFacade(regCenter, jobConfig.getJobName(), this.elasticJobListeners);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig.getJobName());
        this.jobConfig = setUpFacade.setUpJobConfiguration(null == elasticJob ? ScriptJob.class.getName() : elasticJob.getClass().getName(), jobConfig);
        setGuaranteeServiceForElasticJobListeners(regCenter, this.elasticJobListeners);
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final List<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig.getJobName());
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    protected void registerStartUpInfo() {
        JobRegistry.getInstance().registerRegistryCenter(jobConfig.getJobName(), regCenter);
        JobRegistry.getInstance().addJobInstance(jobConfig.getJobName(), new JobInstance());
        JobRegistry.getInstance().setCurrentShardingTotalCount(jobConfig.getJobName(), jobConfig.getShardingTotalCount());
        setUpFacade.registerStartUpInfo(!jobConfig.isDisabled());
    }
    
   /**
    * Shutdown job.
    */
    public final void shutdown() { 
        schedulerFacade.shutdownInstance();
    }
}
