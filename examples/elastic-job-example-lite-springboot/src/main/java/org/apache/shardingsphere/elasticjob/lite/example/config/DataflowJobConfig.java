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
import org.apache.shardingsphere.elasticjob.lite.api.job.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.lite.example.job.dataflow.SpringDataflowJob;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.tracing.api.TracingConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class DataflowJobConfig {
    
    @Resource
    private ZookeeperRegistryCenter regCenter;

    @Resource(name = "tracingConfiguration")
    private TracingConfiguration tracingConfig;
    
    @Bean
    public DataflowJob dataflowJob() {
        return new SpringDataflowJob(); 
    }
    
    @Bean(initMethod = "schedule")
    public ScheduleJobBootstrap dataflowJobScheduler(final DataflowJob dataflowJob, @Value("${dataflowJob.cron}") final String cron, 
                                                     @Value("${dataflowJob.shardingTotalCount}") final int shardingTotalCount,
                                                     @Value("${dataflowJob.shardingItemParameters}") final String shardingItemParameters) {
        return new ScheduleJobBootstrap(regCenter, dataflowJob, getJobConfiguration(dataflowJob.getClass(), cron, shardingTotalCount, shardingItemParameters), tracingConfig);
    }
    
    private JobConfiguration getJobConfiguration(final Class<? extends DataflowJob> jobClass, final String cron, final int shardingTotalCount, final String shardingItemParameters) {
        return JobConfiguration.newBuilder(jobClass.getName(), shardingTotalCount)
                .cron(cron).shardingItemParameters(shardingItemParameters).setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).overwrite(true).build();
    }
}
