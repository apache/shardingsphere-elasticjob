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
import org.apache.shardingsphere.elasticjob.lite.api.job.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.job.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerStatus;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.util.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.util.env.IpUtils;
import org.apache.shardingsphere.elasticjob.lite.util.yaml.YamlEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseIntegrateTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    @Getter(AccessLevel.PROTECTED)
    private final JobConfiguration jobConfiguration;
    
    private final JobBootstrap jobBootstrap;
    
    private final LeaderService leaderService;
    
    @Getter(AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    protected BaseIntegrateTest(final TestType type, final ElasticJob elasticJob) {
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
        regCenter.init();
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
    
    protected final void assertRegCenterCommonInfoWithEnabled() {
        assertRegCenterCommonInfo();
        assertTrue(leaderService.isLeaderUntilBlock());
    }
    
    protected final void assertRegCenterCommonInfoWithDisabled() {
        assertRegCenterCommonInfo();
    }
    
    private void assertRegCenterCommonInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(jobName), is(3));
        assertThat(JobRegistry.getInstance().getJobInstance(jobName).getIp(), is(IpUtils.getIp()));
        JobConfiguration jobConfig = YamlEngine.unmarshal(regCenter.get("/" + jobName + "/config"), YamlJobConfiguration.class).toJobConfiguration();
        assertThat(jobConfig.getShardingTotalCount(), is(3));
        if (jobBootstrap instanceof ScheduleJobBootstrap) {
            assertThat(jobConfig.getCron(), is("0/1 * * * * ?"));
        } else {
            assertNull(jobConfig.getCron());
        }
        assertThat(jobConfig.getShardingItemParameters(), is("0=A,1=B,2=C"));
        if (jobConfiguration.isDisabled()) {
            assertThat(regCenter.get("/" + jobName + "/servers/" + JobRegistry.getInstance().getJobInstance(jobName).getIp()), is(ServerStatus.DISABLED.name()));
            while (null != regCenter.get("/" + jobName + "/leader/election/instance")) {
                BlockUtils.waitingShortTime();
            }
            regCenter.persist("/" + jobName + "/servers/" + JobRegistry.getInstance().getJobInstance(jobName).getIp(), "");
        } else {
            assertThat(regCenter.get("/" + jobName + "/servers/" + JobRegistry.getInstance().getJobInstance(jobName).getIp()), is(""));
            assertThat(regCenter.get("/" + jobName + "/leader/election/instance"), is(JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId()));
        }
        assertTrue(regCenter.isExisted("/" + jobName + "/instances/" + JobRegistry.getInstance().getJobInstance(jobName).getJobInstanceId()));
        regCenter.remove("/" + jobName + "/leader/election");
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
            super(-1L, -1L);
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
