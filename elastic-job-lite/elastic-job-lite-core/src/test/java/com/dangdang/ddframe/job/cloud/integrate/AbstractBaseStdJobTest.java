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

package com.dangdang.ddframe.job.cloud.integrate;

import com.dangdang.ddframe.job.cloud.api.DataFlowElasticJob;
import com.dangdang.ddframe.job.cloud.api.ElasticJob;
import com.dangdang.ddframe.job.cloud.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.cloud.api.JobScheduler;
import com.dangdang.ddframe.job.cloud.api.config.JobConfiguration;
import com.dangdang.ddframe.job.cloud.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.cloud.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.cloud.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.cloud.internal.env.LocalHostService;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.cloud.internal.server.ServerStatus;
import com.dangdang.ddframe.job.cloud.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.job.cloud.plugin.job.type.integrated.ScriptElasticJob;
import com.dangdang.ddframe.job.cloud.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.dangdang.ddframe.job.cloud.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.cloud.internal.schedule.JobRegistry;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Joiner;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseStdJobTest {
    
    private static final int PORT = 3181;
    
    private static final String TEST_TEMP_DIRECTORY = String.format("target/test_zk_data/%s/", System.nanoTime());
    
    private static final String ZK_CONNECTION_STRING = Joiner.on(":").join("localhost", PORT);
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZK_CONNECTION_STRING, "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
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
        this.disabled = disabled;
        jobConfig = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, jobConfig, new ElasticJobListener() {
            
            @Override
            public void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
                regCenter.persist("/" + jobName + "/listener/every", "test");
            }
            
            @Override
            public void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
            }
        }, new AbstractDistributeOnceElasticJobListener(-1L, -1L) {
            
            @Override
            public void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext) {
                regCenter.persist("/" + jobName + "/listener/once", "test");
            }
            
            @Override
            public void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext) {
            }
        });
        monitorPort = -1;
        leaderElectionService = new LeaderElectionService(regCenter, jobConfig);
    }
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        this.monitorPort = monitorPort;
        jobConfig = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, jobConfig);
        disabled = false;
        leaderElectionService = new LeaderElectionService(regCenter, jobConfig);
    }
    
    @SuppressWarnings("unchecked")
    private JobConfiguration initJobConfig(final Class<? extends ElasticJob> elasticJobClass) {
        if (DataFlowElasticJob.class.isAssignableFrom(elasticJobClass)) {
            return JobConfigurationFactory.createDataFlowJobConfigurationBuilder(jobName, (Class<? extends DataFlowElasticJob>) elasticJobClass, 3, "0/1 * * * * ?")
                    .monitorPort(monitorPort)
                    .shardingItemParameters("0=A,1=B,2=C")
                    .disabled(disabled)
                    .overwrite(true)
                    .build();
        } else if (ScriptElasticJob.class.isAssignableFrom(elasticJobClass)) {
            return JobConfigurationFactory.createScriptJobConfigurationBuilder(jobName, 3, "0/1 * * * * ?", AbstractBaseStdJobTest.class.getResource("/script/test.sh").getPath())
                    .monitorPort(monitorPort)
                    .shardingItemParameters("0=A,1=B,2=C")
                    .disabled(disabled)
                    .overwrite(true)
                    .build();
        } else {
            return JobConfigurationFactory.createSimpleJobConfigurationBuilder(jobName, (Class<? extends AbstractSimpleElasticJob>) elasticJobClass, 3, "0/1 * * * * ?")
                    .monitorPort(monitorPort)
                    .shardingItemParameters("0=A,1=B,2=C")
                    .disabled(disabled)
                    .overwrite(true)
                    .build();
        }
    }
    
    @BeforeClass
    public static void init() {
        zkConfig.setNestedPort(PORT);
        zkConfig.setNestedDataDir(TEST_TEMP_DIRECTORY);
        regCenter.init();
    }
    
    @Before
    public void setUp() {
        ProcessCountStatistics.reset(jobName);
        regCenter.init();
    }
    
    @After
    public void tearDown() throws SchedulerException, NoSuchFieldException {
        ProcessCountStatistics.reset(jobName);
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
        assertThat(regCenter.get("/" + jobName + "/config/shardingTotalCount"), is("3"));
        assertThat(regCenter.get("/" + jobName + "/config/shardingItemParameters"), is("0=A,1=B,2=C"));
        assertThat(regCenter.get("/" + jobName + "/config/cron"), is("0/1 * * * * ?"));
        assertThat(regCenter.get("/" + jobName + "/servers/" + localHostService.getIp() + "/hostName"), is(localHostService.getHostName()));
        if (disabled) {
            assertTrue(regCenter.isExisted("/" + jobName + "/servers/" + localHostService.getIp() + "/disabled"));
            assertNull(regCenter.get("/" + jobName + "/leader/election/host"));
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
