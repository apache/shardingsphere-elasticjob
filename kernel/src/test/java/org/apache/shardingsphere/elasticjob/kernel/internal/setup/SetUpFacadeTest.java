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

package org.apache.shardingsphere.elasticjob.kernel.internal.setup;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.kernel.internal.listener.ListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.reconcile.ReconcileService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetUpFacadeTest {
    
    @Mock
    private LeaderService leaderService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private ReconcileService reconcileService;
    
    @Mock
    private ListenerManager listenerManager;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    private SetUpFacade setUpFacade;
    
    @BeforeEach
    void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        setUpFacade = new SetUpFacade(regCenter, "test_job", Collections.emptyList());
        ReflectionUtils.setFieldValue(setUpFacade, "leaderService", leaderService);
        ReflectionUtils.setFieldValue(setUpFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(setUpFacade, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(setUpFacade, "reconcileService", reconcileService);
        ReflectionUtils.setFieldValue(setUpFacade, "listenerManager", listenerManager);
    }
    
    @Test
    void assertRegisterStartUpInfo() {
        setUpFacade.registerStartUpInfo(true);
        verify(listenerManager).startAllListeners();
        verify(leaderService).electLeader();
        verify(serverService).persistOnline(true);
    }
    
    @Test
    void assertTearDown() {
        when(reconcileService.isRunning()).thenReturn(true);
        setUpFacade.tearDown();
        verify(reconcileService).stopAsync();
        verify(regCenter).removeDataListeners("/test_job");
        verify(regCenter).removeConnStateListener("/test_job");
    }
}
