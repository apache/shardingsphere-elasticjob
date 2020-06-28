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
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.api.JobScheduler;
import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.api.script.ScriptJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration.Builder;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.fixture.EmbedTestingServer;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.SchedulerFacade;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBaseStdJobTest {
    
    private static ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(EmbedTestingServer.getConnectionString(), "zkRegTestCenter");
    
    @Getter(value = AccessLevel.PROTECTED)
    private static CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
    
    @Getter(AccessLevel.PROTECTED)
    private final JobConfiguration jobConfiguration;
    
    private final JobScheduler jobScheduler;
    
    private final boolean disabled;
    
    private final int monitorPort;
    
    private final LeaderService leaderService;
    
    @Getter(AccessLevel.PROTECTED)
    private final String jobName = System.nanoTime() + "_test_job";
    
    @SneakyThrows
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final boolean disabled) {
        this.disabled = disabled;
        jobConfiguration = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, ScriptJob.class == elasticJobClass ? null : elasticJobClass.newInstance(), jobConfiguration, new ElasticJobListener() {
            
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
    
    @SneakyThrows
    protected AbstractBaseStdJobTest(final Class<? extends ElasticJob> elasticJobClass, final int monitorPort) {
        this.monitorPort = monitorPort;
        jobConfiguration = initJobConfig(elasticJobClass);
        jobScheduler = new JobScheduler(regCenter, elasticJobClass.newInstance(), jobConfiguration);
        disabled = false;
        leaderService = new LeaderService(regCenter, jobName);
    }
    
    private JobConfiguration initJobConfig(final Class<? extends ElasticJob> elasticJobClass) {
        String cron = "0/1 * * * * ?";
        int totalShardingCount = 3;
        String shardingParameters = "0=A,1=B,2=C";
        Builder builder = JobConfiguration.newBuilder(jobName, getJobType(elasticJobClass), cron, totalShardingCount).shardingItemParameters(shardingParameters).monitorPort(monitorPort)
                .jobErrorHandlerType("IGNORE").disabled(disabled).overwrite(true);
        if (DataflowJob.class.isAssignableFrom(elasticJobClass)) {
            builder.setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.TRUE.toString());
        } else if (ScriptJob.class.isAssignableFrom(elasticJobClass)) {
            builder.setProperty(ScriptJobExecutor.SCRIPT_KEY, AbstractBaseStdJobTest.class.getResource("/script/test.sh").getPath());
        }
        return builder.build();
    }
    
    private JobType getJobType(final Class<? extends ElasticJob> elasticJobClass) {
        if (DataflowJob.class.isAssignableFrom(elasticJobClass)) {
            return JobType.DATAFLOW;
        }
        if (ScriptJob.class.isAssignableFrom(elasticJobClass)) {
            return JobType.SCRIPT;
        }
        return JobType.SIMPLE;
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
    public void tearDown() {
        ((SchedulerFacade) ReflectionUtils.getFieldValue(jobScheduler, "schedulerFacade")).shutdownInstance();
        ReflectionUtils.setFieldValue(JobRegistry.getInstance(), "instance", null);
    }
    
    protected final void initJob() {
        jobScheduler.init();
    }
    
    final void assertRegCenterCommonInfoWithEnabled() {
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
        assertThat(jobConfig.getShardingItemParameters(), is("0=A,1=B,2=C"));
        assertThat(jobConfig.getCron(), is("0/1 * * * * ?"));
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
