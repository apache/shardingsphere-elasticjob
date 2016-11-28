/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.integrate;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.integrate.fixture.IgnoreJobExceptionHandler;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.server.ServerStatus;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseStdJobTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    @Getter(AccessLevel.PROTECTED)
    private final LocalHostService localHostService = new LocalHostService();
    
    @Getter(AccessLevel.PROTECTED)
    private final LiteJobConfiguration liteJobConfig;
    
    private final JobScheduler jobScheduler;
    
    private final boolean disabled;
    
    private final int monitorPort;
    
    private final LeaderElectionService leaderElectionService;
    
    @Getter(AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final boolean disabled) {
        this.disabled = disabled;
        liteJobConfig = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, liteJobConfig, new ElasticJobListener() {
            
            @Override
            public void beforeJobExecuted(final ShardingContexts shardingContexts) {
                regCenter.persist("/" + jobName + "/listener/every", "test");
            }
            
            @Override
            public void afterJobExecuted(final ShardingContexts shardingContexts) {
            }
        }, new AbstractDistributeOnceElasticJobListener(-1L, -1L) {
            
            @Override
            public void doBeforeJobExecutedAtLastStarted(final ShardingContexts shardingContexts) {
                regCenter.persist("/" + jobName + "/listener/once", "test");
            }
            
            @Override
            public void doAfterJobExecutedAtLastCompleted(final ShardingContexts shardingContexts) {
            }
        });
        monitorPort = -1;
        leaderElectionService = new LeaderElectionService(regCenter, jobName);
    }
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        this.monitorPort = monitorPort;
        liteJobConfig = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, liteJobConfig);
        disabled = false;
        leaderElectionService = new LeaderElectionService(regCenter, jobName);
    }
    
    private LiteJobConfiguration initJobConfig(final Class<? extends ElasticJob> elasticJobClass) {
        String cron = "0/1 * * * * ?";
        int totalShardingCount = 3;
        String shardingParameters = "0=A,1=B,2=C";
        JobCoreConfiguration jobCoreConfig = JobCoreConfiguration.newBuilder(jobName, cron, totalShardingCount).shardingItemParameters(shardingParameters)
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), IgnoreJobExceptionHandler.class.getCanonicalName()).build();
        JobTypeConfiguration jobTypeConfig;
        if (DataflowJob.class.isAssignableFrom(elasticJobClass)) {
            jobTypeConfig = new DataflowJobConfiguration(jobCoreConfig, elasticJobClass.getCanonicalName(), false);
        } else if (ScriptJob.class.isAssignableFrom(elasticJobClass)) {
            jobTypeConfig = new ScriptJobConfiguration(jobCoreConfig, AbstractBaseStdJobTest.class.getResource("/script/test.sh").getPath());
        } else {
            jobTypeConfig = new SimpleJobConfiguration(jobCoreConfig, elasticJobClass.getCanonicalName());
        }
        return LiteJobConfiguration.newBuilder(jobTypeConfig).monitorPort(monitorPort).disabled(disabled).overwrite(true).build();
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
    }
    
    @After
    public void tearDown() throws SchedulerException, NoSuchFieldException {
        JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
        if (null != jobScheduleController) {
            JobRegistry.getInstance().getJobScheduleController(jobName).shutdown();
        }
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    protected void initJob() {
        jobScheduler.init();
    }
    
    void assertRegCenterCommonInfoWithEnabled() {
        assertRegCenterCommonInfo();
        assertTrue(leaderElectionService.isLeader());
    }
    
    protected void assertRegCenterCommonInfoWithDisabled() {
        assertRegCenterCommonInfo();
        assertFalse(leaderElectionService.isLeader());
    }
    
    private void assertRegCenterCommonInfo() {
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.fromJson(regCenter.get("/" + jobName + "/config"));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(regCenter.get("/" + jobName + "/servers/" + localHostService.getIp() + "/hostName"), is(localHostService.getHostName()));
        if (disabled) {
            assertTrue(regCenter.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/disabled"));
            while (null != regCenter.get("/" + jobName + "/leader/election/host")) {
                BlockUtils.waitingShortTime();
            }
        } else {
            assertFalse(regCenter.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/disabled"));
            assertThat(regCenter.get("/" + jobName + "/leader/election/host"), is(localHostService.getIp()));
        }
        assertFalse(regCenter.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/paused"));
        assertThat(regCenter.get("/" + jobName + "/servers/" + localHostService.getIp() + "/status"), CoreMatchers.is(ServerStatus.READY.name()));
        regCenter.remove("/" + jobName + "/leader/election");
    }
    
    void assertRegCenterListenerInfo() {
        assertThat(regCenter.get("/" + jobName + "/listener/once"), is("test"));
        assertThat(regCenter.get("/" + jobName + "/listener/every"), is("test"));
    }
}
