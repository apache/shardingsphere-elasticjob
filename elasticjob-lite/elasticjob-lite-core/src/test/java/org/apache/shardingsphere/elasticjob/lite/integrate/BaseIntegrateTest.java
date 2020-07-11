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

package org.apache.shardingsphere.elasticjob.lite.integrate;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

@Getter(AccessLevel.PROTECTED)
public abstract class BaseIntegrateTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    private final ElasticJob elasticJob;
            
    private final JobConfiguration jobConfiguration;
    
    private final JobBootstrap jobBootstrap;
    
    private final LeaderService leaderService;
    
    private final String jobName = System.nanoTime() + "_test_job";
    
    protected BaseIntegrateTest(final TestType type, final ElasticJob elasticJob) {
        this.elasticJob = elasticJob;
        jobConfiguration = getJobConfiguration(jobName);
        jobBootstrap = createJobBootstrap(type, elasticJob);
        leaderService = new LeaderService(regCenter, jobName);
    }
    
    protected abstract JobConfiguration getJobConfiguration(String jobName);
    
    private JobBootstrap createJobBootstrap(final TestType type, final ElasticJob elasticJob) {
        switch (type) {
            case SCHEDULE:
                return new ScheduleJobBootstrap(regCenter, elasticJob, jobConfiguration, new TestElasticJobListener(), new TestDistributeOnceElasticJobListener());
            case ONE_OFF:
                return new OneOffJobBootstrap(regCenter, elasticJob, jobConfiguration, new TestElasticJobListener(), new TestDistributeOnceElasticJobListener());
            default:
                throw new RuntimeException(String.format("Cannot support `%s`", type));
        }
    }
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        zkConfig.setConnectionTimeoutMilliseconds(30000);
        regCenter.init();
    }
    
    @Before
    public void setUp() {
        if (jobBootstrap instanceof ScheduleJobBootstrap) {
            ((ScheduleJobBootstrap) jobBootstrap).schedule();
        } else {
            ((OneOffJobBootstrap) jobBootstrap).execute();
        }
    }
    
    @After
    public void tearDown() {
        jobBootstrap.shutdown();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    public enum TestType {
        
        SCHEDULE, ONE_OFF
    }
    
    private final class TestElasticJobListener implements ElasticJobListener {
        
        @Override
        public void beforeJobExecuted(final ShardingContexts shardingContexts) {
            regCenter.persist("/" + jobName + "/listener/every", "test");
        }
        
        @Override
        public void afterJobExecuted(final ShardingContexts shardingContexts) {
        }
    }
    
    private final class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
    
        private TestDistributeOnceElasticJobListener() {
            super(100L, 100L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
            regCenter.persist("/" + jobName + "/listener/once", "test");
        }
    
        @Override
        public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
        }
    }
}
