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

package org.apache.shardingsphere.elasticjob.lite.example.config;

import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.job.type.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.api.job.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.example.job.simple.SpringSimpleJob;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class SimpleJobConfig {
    
    @Resource
    private ZookeeperRegistryCenter regCenter;
    
    @Resource
    private TracingConfiguration tracingConfig;
    
    @Bean
    public SimpleJob simpleJob() {
        return new SpringSimpleJob(); 
    }
    
    @Bean(initMethod = "schedule")
    public ScheduleJobBootstrap simpleJobScheduler(final SimpleJob simpleJob, @Value("${simpleJob.cron}") final String cron, @Value("${simpleJob.shardingTotalCount}") final int shardingTotalCount,
                                                   @Value("${simpleJob.shardingItemParameters}") final String shardingItemParameters) {
        return new ScheduleJobBootstrap(regCenter, simpleJob, getJobConfiguration(simpleJob.getClass(), cron, shardingTotalCount, shardingItemParameters), tracingConfig);
    }
    
    private JobConfiguration getJobConfiguration(final Class<? extends SimpleJob> jobClass, final String cron, final int shardingTotalCount, final String shardingItemParameters) {
        return JobConfiguration.newBuilder(jobClass.getName(), shardingTotalCount).cron(cron).shardingItemParameters(shardingItemParameters).overwrite(true).build();
    }
}
