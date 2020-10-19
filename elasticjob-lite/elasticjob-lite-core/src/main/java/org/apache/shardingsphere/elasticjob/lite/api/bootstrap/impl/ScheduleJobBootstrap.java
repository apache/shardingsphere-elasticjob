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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduler;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Schedule job bootstrap.
 */
public final class ScheduleJobBootstrap implements JobBootstrap {
    
    private final JobScheduler jobScheduler;
    
    public ScheduleJobBootstrap(final CoordinatorRegistryCenter regCenter, final ElasticJob elasticJob, final JobConfiguration jobConfig) {
        jobScheduler = new JobScheduler(regCenter, elasticJob, jobConfig);
    }
    
    public ScheduleJobBootstrap(final CoordinatorRegistryCenter regCenter, final String elasticJobType, final JobConfiguration jobConfig) {
        jobScheduler = new JobScheduler(regCenter, elasticJobType, jobConfig);
    }
    
    /**
     * Schedule job.
     */
    public void schedule() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobScheduler.getJobConfig().getCron()), "Cron can not be empty.");
        jobScheduler.getJobScheduleController().scheduleJob(jobScheduler.getJobConfig().getCron());
    }
    
    @Override
    public void shutdown() {
        jobScheduler.shutdown();
    }
}
