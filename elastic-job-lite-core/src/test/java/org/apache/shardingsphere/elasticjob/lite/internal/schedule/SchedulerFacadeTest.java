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

package org.apache.shardingsphere.elasticjob.lite.internal.schedule;

import org.apache.shardingsphere.elasticjob.lite.api.JobType;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.type.impl.DataflowJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.fixture.TestDataflowJob;
import org.apache.shardingsphere.elasticjob.lite.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.lite.internal.monitor.MonitorService;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchedulerFacadeTest {
    
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
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        schedulerFacade = new SchedulerFacade(null, "test_job", Collections.emptyList());
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
        JobConfiguration jobConfig = JobConfiguration.newBuilder(
                "test_job", JobType.DATAFLOW, "0/1 * * * * ?", 3).setProperty(DataflowJobExecutor.STREAM_PROCESS_KEY, Boolean.TRUE.toString()).build();
        when(configService.load(false)).thenReturn(jobConfig);
        assertThat(schedulerFacade.updateJobConfiguration(TestDataflowJob.class.getName(), jobConfig), is(jobConfig));
        verify(configService).persist(TestDataflowJob.class.getName(), jobConfig);
    }
    
    @Test
    public void assertRegisterStartUpInfo() {
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
