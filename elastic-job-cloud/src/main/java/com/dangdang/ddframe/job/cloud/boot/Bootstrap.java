/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.boot;

import com.dangdang.ddframe.job.internal.executor.JobExecutor;
import com.dangdang.ddframe.job.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.api.config.impl.SimpleJobConfiguration;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;

/**
 * Elastic Job Cloud的启动入口.
 *
 * @author zhangliang
 */
public final class Bootstrap {
    
    public static void main(final String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ZookeeperConfiguration jobZkConfig = new ZookeeperConfiguration("localhost:2181", "elastic-job-cloud-example");
        CoordinatorRegistryCenter jobRegCenter = new ZookeeperRegistryCenter(jobZkConfig);
        jobRegCenter.init();
        SimpleJobConfiguration simpleJobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder(
                "simpleElasticDemoJob", (Class<? extends AbstractSimpleElasticJob>) Class.forName("com.dangdang.example.elasticjob.core.job.SimpleJobDemo"), 1, "0/5 * * * * ?").build();
        JobExecutor jobSchedulerInternal = new JobExecutor(jobRegCenter, simpleJobConfig);
        jobSchedulerInternal.init();
        jobSchedulerInternal.getElasticJob().execute();
    }
}
