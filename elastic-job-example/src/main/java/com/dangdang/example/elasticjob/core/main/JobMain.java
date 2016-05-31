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

package com.dangdang.example.elasticjob.core.main;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.api.config.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.config.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.config.SimpleJobConfiguration;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.example.elasticjob.core.job.SequenceDataFlowJobDemo;
import com.dangdang.example.elasticjob.core.job.SimpleJobDemo;
import com.dangdang.example.elasticjob.core.job.ThroughputDataFlowJobDemo;

import static com.dangdang.example.elasticjob.utils.ScriptCommandLineHelper.buildScriptCommandLine;

public final class JobMain {
    
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:4181", "elasticjob-example");
    
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    private final JobConfiguration simpleJobConfig = new SimpleJobConfiguration("simpleElasticDemoJob", SimpleJobDemo.class, 10, "0/30 * * * * ?");
    
    private final JobConfiguration throughputJobConfig = new DataFlowJobConfiguration("throughputDataFlowElasticDemoJob", ThroughputDataFlowJobDemo.class, 10, "0/5 * * * * ?");
    
    private final JobConfiguration sequenceJobConfig = new DataFlowJobConfiguration("sequenceDataFlowElasticDemoJob", SequenceDataFlowJobDemo.class, 10, "0/5 * * * * ?");
    
    private final JobConfiguration scriptElasticJobConfig = new ScriptJobConfiguration("scriptElasticDemoJob", 10, "0/5 * * * * ?", buildScriptCommandLine());
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        new JobMain().init();
    }
    
    public void init() {
        zkConfig.setNestedPort(4181);
        zkConfig.setNestedDataDir(String.format("target/test_zk_data/%s/", System.nanoTime()));
        regCenter.init();
        new JobScheduler(regCenter, simpleJobConfig, new SimpleDistributeOnceElasticJobListener()).init();
        new JobScheduler(regCenter, throughputJobConfig).init();
        new JobScheduler(regCenter, sequenceJobConfig).init();
        new JobScheduler(regCenter, scriptElasticJobConfig).init();
    }
    
    class SimpleDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
        
        SimpleDistributeOnceElasticJobListener() {
            super(1000L, 1000L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext) {
            System.out.println("------ before simple job start ------");
        }
        
        @Override
        public void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext) {
            System.out.println("------ after simple job start ------");
        }
    }
}
