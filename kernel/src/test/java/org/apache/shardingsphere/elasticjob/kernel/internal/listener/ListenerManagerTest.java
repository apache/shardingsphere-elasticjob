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

package org.apache.shardingsphere.elasticjob.kernel.internal.listener;

import org.apache.shardingsphere.elasticjob.kernel.internal.config.RescheduleListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.election.ElectionListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.failover.FailoverListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.guarantee.GuaranteeListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.ShutdownListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.MonitorExecutionListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingListenerManager;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.internal.trigger.TriggerListenerManager;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ElectionListenerManager electionListenerManager;
    
    @Mock
    private ShardingListenerManager shardingListenerManager;
    
    @Mock
    private FailoverListenerManager failoverListenerManager;
    
    @Mock
    private MonitorExecutionListenerManager monitorExecutionListenerManager;
    
    @Mock
    private ShutdownListenerManager shutdownListenerManager;
    
    @Mock
    private TriggerListenerManager triggerListenerManager;
    
    @Mock
    private RescheduleListenerManager rescheduleListenerManager;
    
    @Mock
    private GuaranteeListenerManager guaranteeListenerManager;
    
    @Mock
    private RegistryCenterConnectionStateListener regCenterConnectionStateListener;
    
    private final ListenerManager listenerManager = new ListenerManager(null, "test_job", Collections.emptyList());
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setFieldValue(listenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(listenerManager, "electionListenerManager", electionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "shardingListenerManager", shardingListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "failoverListenerManager", failoverListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "monitorExecutionListenerManager", monitorExecutionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "shutdownListenerManager", shutdownListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "triggerListenerManager", triggerListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "rescheduleListenerManager", rescheduleListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "guaranteeListenerManager", guaranteeListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "regCenterConnectionStateListener", regCenterConnectionStateListener);
    }
    
    @Test
    void assertStartAllListeners() {
        listenerManager.startAllListeners();
        verify(electionListenerManager).start();
        verify(shardingListenerManager).start();
        verify(failoverListenerManager).start();
        verify(monitorExecutionListenerManager).start();
        verify(shutdownListenerManager).start();
        verify(rescheduleListenerManager).start();
        verify(guaranteeListenerManager).start();
        verify(jobNodeStorage).addConnectionStateListener(regCenterConnectionStateListener);
    }
}
