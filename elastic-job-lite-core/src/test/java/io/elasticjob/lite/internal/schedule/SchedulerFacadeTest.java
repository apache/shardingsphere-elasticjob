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

package io.elasticjob.lite.internal.schedule;

import io.elasticjob.lite.api.listener.ElasticJobListener;
import io.elasticjob.lite.api.strategy.JobInstance;
import io.elasticjob.lite.config.JobCoreConfiguration;
import io.elasticjob.lite.config.LiteJobConfiguration;
import io.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import io.elasticjob.lite.fixture.TestDataflowJob;
import io.elasticjob.lite.fixture.util.JobConfigurationUtil;
import io.elasticjob.lite.internal.config.ConfigurationService;
import io.elasticjob.lite.internal.election.LeaderService;
import io.elasticjob.lite.internal.instance.InstanceService;
import io.elasticjob.lite.internal.listener.ListenerManager;
import io.elasticjob.lite.internal.monitor.MonitorService;
import io.elasticjob.lite.internal.reconcile.ReconcileService;
import io.elasticjob.lite.internal.server.ServerService;
import io.elasticjob.lite.internal.sharding.ShardingService;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerFacadeTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private ReconcileService reconcileService;
    
    @Mock
    private ListenerManager listenerManager;
    
    private final LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createDataflowLiteJobConfiguration();
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        MockitoAnnotations.initMocks(this);
        schedulerFacade = new SchedulerFacade(null, "test_job", Collections.<ElasticJobListener>emptyList());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), false)).build());
        ReflectionUtils.setFieldValue(schedulerFacade, "configService", configService);
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(schedulerFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(schedulerFacade, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(schedulerFacade, "monitorService", monitorService);
        ReflectionUtils.setFieldValue(schedulerFacade, "reconcileService", reconcileService);
        ReflectionUtils.setFieldValue(schedulerFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertUpdateJobConfiguration() {
        LiteJobConfiguration jobConfig = LiteJobConfiguration.newBuilder(
                new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(), TestDataflowJob.class.getCanonicalName(), false)).build();
        when(configService.load(false)).thenReturn(jobConfig);
        assertThat(schedulerFacade.updateJobConfiguration(jobConfig), is(jobConfig));
        verify(configService).persist(jobConfig);
    }
    
    @Test
    public void assertRegisterStartUpInfo() {
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), false)).build());
        schedulerFacade.registerStartUpInfo(true);
        verify(listenerManager).startAllListeners();
        verify(leaderService).electLeader();
        verify(serverService).persistOnline(true);
        verify(shardingService).setReshardingFlag();
        verify(monitorService).listen();
    }
    
    @Test
    public void assertShutdownInstanceIfNotLeaderAndReconcileServiceIsNotRunning() {
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        schedulerFacade.shutdownInstance();
        verify(leaderService, times(0)).removeLeader();
        verify(monitorService).close();
        verify(reconcileService, times(0)).stopAsync();
        verify(jobScheduleController).shutdown();
    }
    
    @Test
    public void assertShutdownInstanceIfLeaderAndReconcileServiceIsRunning() {
        when(leaderService.isLeader()).thenReturn(true);
        when(reconcileService.isRunning()).thenReturn(true);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController, regCenter);
        schedulerFacade.shutdownInstance();
        verify(leaderService).removeLeader();
        verify(monitorService).close();
        verify(reconcileService).stopAsync();
        verify(jobScheduleController).shutdown();
    }
}
