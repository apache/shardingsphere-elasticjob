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

package org.apache.shardingsphere.elasticjob.kernel.executor.facade;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.failover.FailoverService;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionContextService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.listener.fixture.ElasticJobListenerCaller;
import org.apache.shardingsphere.elasticjob.kernel.listener.fixture.TestElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.tracing.event.JobTracingEventBus;
import org.apache.shardingsphere.elasticjob.spi.listener.param.ShardingContexts;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SingleShardingJobFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private JobTracingEventBus jobTracingEventBus;
    
    @Mock
    private ElasticJobListenerCaller caller;

    @Mock
    private JobNodeStorage jobNodeStorage;

    @Mock
    private InstanceService instanceService;
    
    private SingleShardingJobFacade singleShardingJobFacade;
    
    private StringBuilder orderResult;
    
    @BeforeEach
    void setUp() {
        orderResult = new StringBuilder();
        singleShardingJobFacade = new SingleShardingJobFacade(null, "test_job",
                Arrays.asList(new TestElasticJobListener(caller, "l1", 2, orderResult), new TestElasticJobListener(caller, "l2", 1, orderResult)), null);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "configService", configService);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "shardingService", shardingService);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "executionContextService", executionContextService);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "executionService", executionService);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "failoverService", failoverService);
        ReflectionUtils.setSuperclassFieldValue(singleShardingJobFacade, "jobTracingEventBus", jobTracingEventBus);
        ReflectionUtils.setFieldValue(singleShardingJobFacade, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(singleShardingJobFacade, "instanceService", instanceService);
    }
    
    @Test
    void assertLoad() {
        JobConfiguration expected = JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").build();
        when(configService.load(true)).thenReturn(expected);
        assertThat(singleShardingJobFacade.loadJobConfiguration(true), is(expected));
    }
    
    @Test
    void assertCheckMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        singleShardingJobFacade.checkJobExecutionEnvironment();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    void assertFailoverIfUnnecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        singleShardingJobFacade.failoverIfNecessary();
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    void assertFailoverIfNecessary() {
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        singleShardingJobFacade.failoverIfNecessary();
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    void assertRegisterJobBegin() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        singleShardingJobFacade.registerJobBegin(shardingContexts);
        verify(executionService).registerJobBegin(shardingContexts);
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverDisabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        singleShardingJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }
    
    @Test
    void assertRegisterJobCompletedWhenFailoverEnabled() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        singleShardingJobFacade.registerJobCompleted(shardingContexts);
        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
    }

    @Test
    void assertRegisterJobCompletedWhenRunningOnCurrentHost() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        JobInstance jobInstance = new JobInstance();
        jobInstance.setServerIp("192.168.1.2");
        JobRegistry jobRegistry = JobRegistry.getInstance();
        jobRegistry.addJobInstance("test_job", jobInstance);
        List<JobInstance> availJobInst = new ArrayList<>();
        availJobInst.add(jobInstance);
        JobInstance jobInstance2 = new JobInstance();
        jobInstance2.setServerIp("192.168.1.3");
        availJobInst.add(jobInstance2);
        when(instanceService.getAvailableJobInstances()).thenReturn(availJobInst);

        singleShardingJobFacade.registerJobCompleted(shardingContexts);

        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        verify(jobNodeStorage).fillEphemeralJobNode("next-job-instance-ip", availJobInst.get(1).getServerIp());
    }

    @Test
    void assertRegisterJobCompletedWhenRunningOnOtherHost() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        JobInstance jobInstance = new JobInstance();
        jobInstance.setServerIp("192.168.1.2");
        JobRegistry jobRegistry = JobRegistry.getInstance();
        jobRegistry.addJobInstance("test_job", jobInstance);
        List<JobInstance> availJobInst = new ArrayList<>();
        JobInstance jobInstance2 = new JobInstance();
        jobInstance2.setServerIp("192.168.1.3");
        availJobInst.add(jobInstance2);
        when(instanceService.getAvailableJobInstances()).thenReturn(availJobInst);

        singleShardingJobFacade.registerJobCompleted(shardingContexts);

        verify(executionService).registerJobCompleted(shardingContexts);
        verify(failoverService).updateFailoverComplete(shardingContexts.getShardingItemParameters().keySet());
        verify(jobNodeStorage, times(0)).fillEphemeralJobNode("next-job-instance-ip", availJobInst.get(0).getServerIp());
    }

    @Test
    void assertGetShardingContextWhenIsFailoverEnableAndFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);
        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService, times(0)).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverEnableAndNotFailover() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(true).monitorExecution(true).build());
        when(failoverService.getLocalFailoverItems()).thenReturn(Collections.emptyList());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0));
        when(failoverService.getLocalTakeOffItems()).thenReturn(Collections.emptyList());
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);
        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenIsFailoverDisable() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);
        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    void assertGetShardingContextWhenHasDisabledItems() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Lists.newArrayList(0));
        when(executionService.getDisabledItems(Collections.singletonList(0))).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.emptyList())).thenReturn(shardingContexts);
        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));
        verify(shardingService).shardingIfNecessary();
    }

    @Test
    void assertGetShardingContextWhenIsFailoverDisableAndNoNeedShardingWithoutNextIP() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("next-job-instance-ip")).thenReturn(null);
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);

        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));

        verify(shardingService, times(1)).shardingIfNecessary();
    }

    @Test
    void assertGetShardingContextWhenIsFailoverDisableAndNoNeedShardingWithNextIP() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(jobNodeStorage.getJobNodeDataDirectly("next-job-instance-ip")).thenReturn("192.168.1.2");
        JobInstance jobInstance = new JobInstance();
        jobInstance.setServerIp("192.168.1.2");
        JobRegistry jobRegistry = JobRegistry.getInstance();
        jobRegistry.addJobInstance("test_job", jobInstance);
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);

        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));

        verify(shardingService, times(0)).shardingIfNecessary();
    }

    @Test
    void assertGetShardingContextWhenIsFailoverDisableAndNeedSharding() {
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap());
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 1).cron("0/1 * * * * ?").failover(false).build());
        when(shardingService.getLocalShardingItems()).thenReturn(Collections.singletonList(0));
        when(shardingService.isNeedSharding()).thenReturn(true);
        when(executionContextService.getJobShardingContext(Collections.singletonList(0))).thenReturn(shardingContexts);

        assertThat(singleShardingJobFacade.getShardingContexts(), is(shardingContexts));

        verify(shardingService).shardingIfNecessary();
    }

    @Test
    void assertMisfireIfRunning() {
        when(executionService.misfireIfHasRunningItems(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(singleShardingJobFacade.misfireIfRunning(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    void assertClearMisfire() {
        singleShardingJobFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    void assertIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(singleShardingJobFacade.isNeedSharding(), is(true));
    }
    
    @Test
    void assertBeforeJobExecuted() {
        singleShardingJobFacade.beforeJobExecuted(new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap()));
        verify(caller, times(2)).before();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertAfterJobExecuted() {
        singleShardingJobFacade.afterJobExecuted(new ShardingContexts("fake_task_id", "test_job", 1, "", Collections.emptyMap()));
        verify(caller, times(2)).after();
        assertThat(orderResult.toString(), is("l2l1"));
    }
    
    @Test
    void assertPostJobExecutionEvent() {
        singleShardingJobFacade.postJobExecutionEvent(null);
        verify(jobTracingEventBus).post(null);
    }
}
