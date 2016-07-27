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

import com.dangdang.ddframe.job.api.JobConfigurationFactory;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.type.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.api.type.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.api.type.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.example.elasticjob.core.job.SequenceDataflowJobDemo;
import com.dangdang.example.elasticjob.core.job.SimpleJobDemo;
import com.dangdang.example.elasticjob.core.job.ThroughputDataflowJobDemo;

import static com.dangdang.example.elasticjob.utils.ScriptCommandLineHelper.buildScriptCommandLine;

public final class JobMain {
    
    private final ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:4181", "elasticjob-example");
    
    private final CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
    // CHECKSTYLE:ON
        new JobMain().init();
    }
    
    private void init() {
        zkConfig.setNestedPort(4181);
        zkConfig.setNestedDataDir(String.format("target/test_zk_data/%s/", System.nanoTime()));
        regCenter.init();
        
        final SimpleJobConfiguration simpleJobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("simpleElasticDemoJob", SimpleJobDemo.class, "0/30 * * * * ?", 10).build();
        
        final DataflowJobConfiguration throughputJobConfig = JobConfigurationFactory.createDataflowJobConfigurationBuilder(
                "throughputDataflowElasticDemoJob", ThroughputDataflowJobDemo.class, "0/5 * * * * ?", 10, DataflowJobConfiguration.DataflowType.THROUGHPUT).streamingProcess(true).build();
        
        final DataflowJobConfiguration sequenceJobConfig = JobConfigurationFactory.createDataflowJobConfigurationBuilder(
                "sequenceDataflowElasticDemoJob", SequenceDataflowJobDemo.class, "0/5 * * * * ?", 10, DataflowJobConfiguration.DataflowType.SEQUENCE).build();
        
        final ScriptJobConfiguration scriptJobConfig = JobConfigurationFactory.createScriptJobConfigurationBuilder(
                "scriptElasticDemoJob", "0/5 * * * * ?", 10, buildScriptCommandLine()).build();
        
        new JobScheduler(regCenter, LiteJobConfiguration.createBuilder(simpleJobConfig).build(), new SimpleDistributeOnceElasticJobListener()).init();
        new JobScheduler(regCenter, LiteJobConfiguration.createBuilder(throughputJobConfig).build()).init();
        new JobScheduler(regCenter, LiteJobConfiguration.createBuilder(sequenceJobConfig).build()).init();
        new JobScheduler(regCenter, LiteJobConfiguration.createBuilder(scriptJobConfig).build()).init();
    }
    
    private class SimpleDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
        
        SimpleDistributeOnceElasticJobListener() {
            super(1000L, 1000L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final ShardingContext shardingContext) {
            System.out.println("------ before simple job start ------");
        }
        
        @Override
        public void doAfterJobExecutedAtLastCompleted(final ShardingContext shardingContext) {
            System.out.println("------ after simple job start ------");
        }
    }
}
