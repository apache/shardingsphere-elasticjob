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

package com.dangdang.ddframe.job.integrate;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.internal.server.ServerStatus;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.reg.AbstractNestedZookeeperBaseTest;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.AccessLevel;
import lombok.Getter;
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

public abstract class AbstractBaseStdJobTest extends AbstractNestedZookeeperBaseTest {
    
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
        jobConfig = new JobConfiguration(jobName, elasticJobClass, 3, "0/1 * * * * ?");
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
        this.disabled = disabled;
        monitorPort = -1;
        leaderElectionService = new LeaderElectionService(regCenter, jobConfig);
    }
    
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        jobConfig = new JobConfiguration(jobName, elasticJobClass, 3, "0/1 * * * * ?");
        jobScheduler = new JobScheduler(regCenter, jobConfig);
        disabled = false;
        this.monitorPort = monitorPort;
        leaderElectionService = new LeaderElectionService(regCenter, jobConfig);
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
        jobConfig.setShardingItemParameters("0=A,1=B,2=C");
        jobConfig.setDisabled(disabled);
        jobConfig.setMonitorPort(monitorPort);
        jobConfig.setOverwrite(true);
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
    
    protected void assertRegCenterCommonInfoWithEnabled() {
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
        assertThat(regCenter.get("/" + jobName + "/servers/" + localHostService.getIp() + "/status"), is(ServerStatus.READY.name()));
        regCenter.remove("/" + jobName + "/leader/election");
    }
    
    protected void assertRegCenterListenerInfo() {
        assertThat(regCenter.get("/" + jobName + "/listener/once"), is("test"));
        assertThat(regCenter.get("/" + jobName + "/listener/every"), is("test"));
    }
}
