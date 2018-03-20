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

package io.elasticjob.lite.integrate;

import io.elasticjob.lite.api.ElasticJob;
import io.elasticjob.lite.api.JobScheduler;
import io.elasticjob.lite.api.dataflow.DataflowJob;
import io.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.api.script.ScriptJob;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.JobTypeConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.config.script.ScriptJobConfiguration;
import io.elasticjob.lite.config.simple.SimpleJobConfiguration;
import io.elasticjob.lite.executor.ShardingContexts;
import io.elasticjob.lite.executor.handler.JobProperties;
import io.elasticjob.lite.fixture.EmbedTestingServer;
import io.elasticjob.lite.integrate.fixture.IgnoreJobExceptionHandler;
import io.elasticjob.lite.internal.config.LiteJobConfigurationGsonFactory;
import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.server.ServerStatus;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import io.elasticjob.lite.reg.zookeeper.ZookeeperConfiguration;
import io.elasticjob.lite.reg.zookeeper.ZookeeperRegistryCenter;
import io.elasticjob.lite.util.concurrent.BlockUtils;
import io.elasticjob.lite.util.env.IpUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.quartz.SchedulerException;
import org.unitils.util.ReflectionUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseStdJobTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    @Getter(AccessLevel.PROTECTED)
    private final LiteJobConfiguration liteJobConfig;
    
    private final JobScheduler jobScheduler;
    
    private final boolean disabled;
    
    private final int monitorPort;
    
    private final LeaderService leaderService;
    
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
        leaderService = new LeaderService(regCenter, jobName);
    }
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        this.monitorPort = monitorPort;
        liteJobConfig = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, liteJobConfig);
        disabled = false;
        leaderService = new LeaderService(regCenter, jobName);
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
        jobScheduler.getSchedulerFacade().shutdownInstance();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
        
    }
    
    protected void initJob() {
        jobScheduler.init();
    }
    
    void assertRegCenterCommonInfoWithEnabled() {
        assertRegCenterCommonInfo();
        assertTrue(leaderService.isLeaderUntilBlock());
    }
    
    protected void assertRegCenterCommonInfoWithDisabled() {
        assertRegCenterCommonInfo();
    }
    
    private void assertRegCenterCommonInfo() {
        assertThat(JobRegistry.getInstance().getCurrentShardingTotalCount(jobName), is(3));
        assertThat(JobRegistry.getInstance().getJobInstance(jobName).getIp(), is(IpUtils.getIp()));
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.fromJson(regCenter.get("/" + jobName + "/config"));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(liteJobConfig.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        if (disabled) {
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
}
