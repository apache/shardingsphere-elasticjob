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

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private LeaderService leaderService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ReconcileService reconcileService;
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        schedulerFacade = new SchedulerFacade(null, "test_job");
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(schedulerFacade, "reconcileService", reconcileService);
    }
    
    @Test
    public void assertShutdownInstanceIfNotLeaderAndReconcileServiceIsNotRunning() {
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        schedulerFacade.shutdownInstance();
        verify(leaderService, times(0)).removeLeader();
        verify(reconcileService, times(0)).stopAsync();
        verify(jobScheduleController).shutdown();
    }
    
    @Test
    public void assertShutdownInstanceIfLeaderAndReconcileServiceIsRunning() {
        when(leaderService.isLeader()).thenReturn(true);
        when(reconcileService.isRunning()).thenReturn(true);
        JobRegistry.getInstance().registerRegistryCenter("test_job", regCenter);
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        schedulerFacade.shutdownInstance();
        verify(leaderService).removeLeader();
        verify(reconcileService).stopAsync();
        verify(jobScheduleController).shutdown();
    }
}
