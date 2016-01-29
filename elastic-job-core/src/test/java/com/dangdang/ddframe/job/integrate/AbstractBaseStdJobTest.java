/**
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

package com.dangdang.ddframe.job.integrate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.test.NestedZookeeperServers;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class AbstractBaseStdJobTest {
    
    protected static final CoordinatorRegistryCenter REG_CENTER = new ZookeeperRegistryCenter(
            new ZookeeperConfiguration(NestedZookeeperServers.ZK_CONNECTION_STRING, "zkRegTestCenter", 1000, 3000, 3));
    
    @Getter(AccessLevel.PROTECTED)
    private final LocalHostService localHostService = new LocalHostService();
    
    @Getter(AccessLevel.PROTECTED)
    private final JobConfiguration jobConfig;
    
    private final JobScheduler jobScheduler;
    
    private final boolean disabled;
    
    private final int monitorPort;
    
    private final LeaderElectionService leaderElectionService;
    
    @Getter(AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_testJob";
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final boolean disabled) {
        jobConfig = new JobConfiguration(jobName, elasticJobClass, 3, "0/1 * * * * ?");
        jobScheduler = new JobScheduler(REG_CENTER, jobConfig);
        this.disabled = disabled;
        monitorPort = -1;
        leaderElectionService = new LeaderElectionService(REG_CENTER, jobConfig);
    }
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        jobConfig = new JobConfiguration(jobName, elasticJobClass, 3, "0/1 * * * * ?");
        jobScheduler = new JobScheduler(REG_CENTER, jobConfig);
        disabled = false;
        this.monitorPort = monitorPort;
        leaderElectionService = new LeaderElectionService(REG_CENTER, jobConfig);
    }
    
    @BeforeClass
    public static void init() {
        NestedZookeeperServers.getInstance().startServerIfNotStarted();
        REG_CENTER.init();
    }
    
    @AfterClass
    public static void destory() {
        REG_CENTER.close();
    }
    
    @Before
    public void setUp() {
        ProcessCountStatistics.reset(jobName);
        jobConfig.setShardingItemParameters("0=A,1=B,2=C");
        jobConfig.setDisabled(disabled);
        jobConfig.setMonitorPort(monitorPort);
        jobConfig.setOverwrite(true);
        REG_CENTER.init();
    }
    
    @After
    public void tearDown() throws SchedulerException, NoSuchFieldException {
        ProcessCountStatistics.reset(jobName);
        JobScheduler jobScheduler = JobRegistry.getInstance().getJobScheduler(jobName);
        if (null != jobScheduler) {
            JobRegistry.getInstance().getJobScheduler(jobName).shutdown();
        }
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    protected void initJob() {
        jobScheduler.init();
    }
    
    protected void assertRegCenterCommonInfo() {
        assertThat(REG_CENTER.get("/" + jobName + "/leader/election/host"), is(localHostService.getIp()));
        assertThat(REG_CENTER.get("/" + jobName + "/config/shardingTotalCount"), is("3"));
        assertThat(REG_CENTER.get("/" + jobName + "/config/shardingItemParameters"), is("0=A,1=B,2=C"));
        assertThat(REG_CENTER.get("/" + jobName + "/config/cron"), is("0/1 * * * * ?"));
        assertThat(REG_CENTER.get("/" + jobName + "/servers/" + localHostService.getIp() + "/hostName"), is(localHostService.getHostName()));
        if (disabled) {
            assertTrue(REG_CENTER.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/disabled"));
        } else {
            assertFalse(REG_CENTER.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/disabled"));
        }
        assertFalse(REG_CENTER.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/stoped"));
        assertThat(REG_CENTER.get("/" + jobName + "/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
        REG_CENTER.remove("/" + jobName + "/leader/election");
        assertTrue(leaderElectionService.isLeader());
    }
    
    public static class TestJob extends AbstractElasticJob {
        
        @Override
        protected void executeJob(final JobExecutionMultipleShardingContext jobExecutionShardingContext) {
        }
    }
}
