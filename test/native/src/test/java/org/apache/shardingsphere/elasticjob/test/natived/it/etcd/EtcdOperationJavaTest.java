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

package org.apache.shardingsphere.elasticjob.test.natived.it.etcd;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.bootstrap.type.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.kernel.tracing.config.TracingConfiguration;
import org.apache.shardingsphere.elasticjob.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.api.JobOperateAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.api.ServerStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.api.ShardingOperateAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.api.ShardingStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.lifecycle.domain.ServerBriefInfo;
import org.apache.shardingsphere.elasticjob.lifecycle.domain.ShardingInfo;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.operate.ShardingOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.settings.JobConfigurationAPIImpl;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.statistics.JobStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.statistics.ServerStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.lifecycle.internal.statistics.ShardingStatisticsAPIImpl;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdConfiguration;
import org.apache.shardingsphere.elasticjob.reg.etcd.EtcdRegistryCenter;
import org.apache.shardingsphere.elasticjob.test.natived.commons.job.simple.JavaSimpleJob;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class EtcdOperationJavaTest {
    
    private static GenericContainer<?> etcdContainer;
    
    private static String serverLists;
    
    private static TracingConfiguration<DataSource> tracingConfig;
    
    private CoordinatorRegistryCenter firstRegCenter;
    
    private CoordinatorRegistryCenter secondRegCenter;
    
    @BeforeAll
    static void beforeAll() {
        etcdContainer = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.18"))
                .withExposedPorts(2379)
                .withCommand("etcd", "--advertise-client-urls", "http://0.0.0.0:2379", "--listen-client-urls", "http://0.0.0.0:2379")
                .waitingFor(Wait.forLogMessage(".*ready to serve client requests.*", 1));
        etcdContainer.start();
        serverLists = "http://127.0.0.1:" + etcdContainer.getMappedPort(2379);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:job_event_storage");
        config.setUsername("sa");
        config.setPassword("");
        tracingConfig = new TracingConfiguration<>("RDB", new HikariDataSource(config));
    }
    
    @AfterAll
    static void afterAll() {
        if (null != etcdContainer) {
            etcdContainer.stop();
        }
    }
    
    @BeforeEach
    void beforeEach() {
        firstRegCenter = new EtcdRegistryCenter(new EtcdConfiguration(serverLists, "elasticjob-test-native-etcd-operation-java"));
        firstRegCenter.init();
        secondRegCenter = new EtcdRegistryCenter(new EtcdConfiguration(serverLists, "elasticjob-test-native-etcd-operation-java"));
        secondRegCenter.init();
    }
    
    @AfterEach
    void afterEach() {
        firstRegCenter.close();
        secondRegCenter.close();
        CoordinatorRegistryCenter cleanupRegCenter = new EtcdRegistryCenter(new EtcdConfiguration(serverLists, "elasticjob-test-native-etcd-operation-cleanup"));
        cleanupRegCenter.init();
        try {
            cleanupRegCenter.remove("/");
        } finally {
            cleanupRegCenter.close();
        }
    }
    
    @Test
    void testJobConfigurationAPI() {
        String jobName = "testEtcdJobConfigurationAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        JobConfigurationAPI jobConfigAPI = new JobConfigurationAPIImpl(secondRegCenter);
        JobConfigurationPOJO jobConfig = jobConfigAPI.getJobConfiguration(jobName);
        assertThat(jobConfig, notNullValue());
        assertThat(jobConfig.getJobName(), is(jobName));
        assertThat(jobConfig.getCron(), is("0/5 * * * * ?"));
        assertThat(jobConfig.getShardingItemParameters(), is("0=Norddorf,1=Bordeaux,2=Somerset"));
        JobConfigurationPOJO newJobConfig = new JobConfigurationPOJO();
        newJobConfig.setJobName(jobConfig.getJobName());
        newJobConfig.setShardingTotalCount(jobConfig.getShardingTotalCount());
        newJobConfig.setCron("0/10 * * * * ?");
        newJobConfig.setShardingItemParameters(jobConfig.getShardingItemParameters());
        newJobConfig.setJobExtraConfigurations(jobConfig.getJobExtraConfigurations());
        jobConfigAPI.updateJobConfiguration(newJobConfig);
        JobConfigurationPOJO newTestJavaSimpleJob = jobConfigAPI.getJobConfiguration(jobName);
        assertThat(newTestJavaSimpleJob, notNullValue());
        assertThat(newTestJavaSimpleJob.getCron(), is("0/10 * * * * ?"));
        jobConfigAPI.removeJobConfiguration(jobName);
        assertThat(jobConfigAPI.getJobConfiguration(jobName), nullValue());
        job.shutdown();
    }
    
    @Test
    void testJobOperateAPI() {
        String jobName = "testEtcdJobOperateAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        List<ServerBriefInfo> serverBriefInfos = new ArrayList<>(new ServerStatisticsAPIImpl(secondRegCenter).getAllServersBriefInfo());
        assertThat(serverBriefInfos.size(), is(1));
        String serverIp = serverBriefInfos.get(0).getServerIp();
        JobOperateAPI jobOperateAPI = new JobOperateAPIImpl(secondRegCenter);
        jobOperateAPI.disable(jobName, serverIp);
        JobStatisticsAPIImpl jobStatisticsAPI = new JobStatisticsAPIImpl(secondRegCenter);
        JobBriefInfo firstJobBriefInfo = jobStatisticsAPI.getJobBriefInfo(jobName);
        assertThat(firstJobBriefInfo, notNullValue());
        assertThat(firstJobBriefInfo.getStatus(), is(JobBriefInfo.JobStatus.DISABLED));
        jobOperateAPI.enable(jobName, serverIp);
        JobBriefInfo secondJobBriefInfo = jobStatisticsAPI.getJobBriefInfo(jobName);
        assertThat(secondJobBriefInfo, notNullValue());
        assertThat(secondJobBriefInfo.getStatus(), is(JobBriefInfo.JobStatus.SHARDING_FLAG));
        jobOperateAPI.remove(jobName, serverIp);
        JobBriefInfo thirdJobBriefInfo = jobStatisticsAPI.getJobBriefInfo(jobName);
        assertThat(thirdJobBriefInfo, notNullValue());
        assertThat(thirdJobBriefInfo.getStatus(), is(JobBriefInfo.JobStatus.CRASHED));
        job.shutdown();
    }
    
    @Test
    void testShardingOperateAPI() {
        String jobName = "testEtcdShardingOperateAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        ShardingOperateAPI shardingOperateAPI = new ShardingOperateAPIImpl(secondRegCenter);
        ShardingStatisticsAPI shardingStatisticsAPI = new ShardingStatisticsAPIImpl(secondRegCenter);
        Awaitility.await()
                .atMost(1L, TimeUnit.MINUTES)
                .ignoreExceptions()
                .until(() -> 3 == shardingStatisticsAPI.getShardingInfo(jobName).size());
        shardingOperateAPI.disable(jobName, "0");
        Awaitility.await()
                .atMost(1L, TimeUnit.MINUTES)
                .ignoreExceptions()
                .until(() -> {
                    List<ShardingInfo> shardingInfos = shardingStatisticsAPI.getShardingInfo(jobName)
                            .stream()
                            .filter(shardingInfo -> 0 == shardingInfo.getItem())
                            .collect(Collectors.toList());
                    return 1 == shardingInfos.size() && ShardingInfo.ShardingStatus.DISABLED == shardingInfos.get(0).getStatus();
                });
        List<ShardingInfo> firstShardingInfos = shardingStatisticsAPI.getShardingInfo(jobName)
                .stream()
                .filter(shardingInfo -> 0 == shardingInfo.getItem())
                .collect(Collectors.toList());
        assertThat(firstShardingInfos.size(), is(1));
        assertThat(firstShardingInfos.get(0).getStatus(), is(ShardingInfo.ShardingStatus.DISABLED));
        shardingOperateAPI.enable(jobName, "0");
        Awaitility.await()
                .atMost(1L, TimeUnit.MINUTES)
                .ignoreExceptions()
                .until(() -> {
                    List<ShardingInfo> shardingInfos = shardingStatisticsAPI.getShardingInfo(jobName)
                            .stream()
                            .filter(shardingInfo -> 0 == shardingInfo.getItem())
                            .collect(Collectors.toList());
                    return 1 == shardingInfos.size() && ShardingInfo.ShardingStatus.SHARDING_FLAG == shardingInfos.get(0).getStatus();
                });
        List<ShardingInfo> secondShardingInfos = shardingStatisticsAPI.getShardingInfo(jobName)
                .stream()
                .filter(shardingInfo -> 0 == shardingInfo.getItem())
                .collect(Collectors.toList());
        assertThat(secondShardingInfos.size(), is(1));
        assertThat(secondShardingInfos.get(0).getStatus(), is(ShardingInfo.ShardingStatus.SHARDING_FLAG));
        job.shutdown();
    }
    
    @Test
    void testJobStatisticsAPI() {
        String jobName = "testEtcdJobStatisticsAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        JobStatisticsAPI jobStatisticsAPI = new JobStatisticsAPIImpl(secondRegCenter);
        assertThat(jobStatisticsAPI.getJobsTotalCount(), is(1));
        JobBriefInfo jobBriefInfo = jobStatisticsAPI.getJobBriefInfo(jobName);
        assertThat(jobBriefInfo, notNullValue());
        assertThat(jobBriefInfo.getJobName(), is(jobName));
        assertThat(jobBriefInfo.getStatus(), is(JobBriefInfo.JobStatus.SHARDING_FLAG));
        assertThat(jobBriefInfo.getDescription(), is(""));
        assertThat(jobBriefInfo.getCron(), is("0/5 * * * * ?"));
        assertThat(jobBriefInfo.getInstanceCount(), is(1));
        assertThat(jobBriefInfo.getShardingTotalCount(), is(3));
        assertThat(jobStatisticsAPI.getAllJobsBriefInfo().size(), is(1));
        assertDoesNotThrow(() -> {
            List<String> ipList = secondRegCenter.getChildrenKeys("/" + jobName + "/servers");
            assertThat(ipList.size(), is(1));
            assertThat(jobStatisticsAPI.getJobsBriefInfo(ipList.get(0)).size(), is(1));
        });
        job.shutdown();
    }
    
    @Test
    void testServerStatisticsAPI() {
        String jobName = "testEtcdServerStatisticsAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        ServerStatisticsAPI serverStatisticsAPI = new ServerStatisticsAPIImpl(secondRegCenter);
        assertThat(serverStatisticsAPI.getServersTotalCount(), is(1));
        Collection<ServerBriefInfo> allServersBriefInfo = serverStatisticsAPI.getAllServersBriefInfo();
        assertThat(allServersBriefInfo.size(), is(1));
        allServersBriefInfo.stream().findFirst().ifPresent(serverBriefInfo -> {
            String serverIp = serverBriefInfo.getServerIp();
            assertThat(serverIp, notNullValue());
            Set<String> instances = serverBriefInfo.getInstances();
            assertThat(instances.size(), is(1));
            assertThat(instances.stream().findFirst().isPresent(), is(true));
            assertThat(instances.stream().findFirst().get(), startsWith(serverIp + "@-@"));
            Set<String> jobNames = serverBriefInfo.getJobNames();
            assertThat(jobNames.size(), is(1));
            assertThat(jobNames.stream().findFirst().isPresent(), is(true));
            assertThat(jobNames.stream().findFirst().get(), is(jobName));
            assertThat(serverBriefInfo.getInstancesNum(), is(1));
            assertThat(serverBriefInfo.getJobsNum(), is(1));
            assertThat(serverBriefInfo.getDisabledJobsNum().intValue(), is(0));
        });
        job.shutdown();
    }
    
    @Test
    void testShardingStatisticsAPI() {
        String jobName = "testEtcdShardingStatisticsAPI";
        ScheduleJobBootstrap job = new ScheduleJobBootstrap(firstRegCenter, new JavaSimpleJob(),
                JobConfiguration.newBuilder(jobName, 3)
                        .cron("0/5 * * * * ?")
                        .shardingItemParameters("0=Norddorf,1=Bordeaux,2=Somerset")
                        .addExtraConfigurations(tracingConfig)
                        .build());
        job.schedule();
        ShardingStatisticsAPI shardingStatisticsAPI = new ShardingStatisticsAPIImpl(secondRegCenter);
        Awaitility.await()
                .atMost(1L, TimeUnit.MINUTES)
                .ignoreExceptions()
                .until(() -> {
                    if (3 != shardingStatisticsAPI.getShardingInfo(jobName).size()) {
                        return false;
                    }
                    return shardingStatisticsAPI.getShardingInfo(jobName).stream().allMatch(shardingInfo -> null != shardingInfo.getServerIp() && null != shardingInfo.getInstanceId()
                            && ShardingInfo.ShardingStatus.SHARDING_FLAG != shardingInfo.getStatus()
                            && ShardingInfo.ShardingStatus.DISABLED != shardingInfo.getStatus());
                });
        shardingStatisticsAPI.getShardingInfo(jobName).forEach(shardingInfo -> {
            String serverIp = shardingInfo.getServerIp();
            assertThat(serverIp, notNullValue());
            assertThat(shardingInfo.getInstanceId(), startsWith(serverIp + "@-@"));
            ShardingInfo.ShardingStatus status = shardingInfo.getStatus();
            assertThat(status, not(ShardingInfo.ShardingStatus.SHARDING_FLAG));
            assertThat(status, not(ShardingInfo.ShardingStatus.DISABLED));
            assertThat(shardingInfo.isFailover(), is(false));
        });
        job.shutdown();
    }
}
