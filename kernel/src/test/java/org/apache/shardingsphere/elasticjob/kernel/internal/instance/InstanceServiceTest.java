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

package org.apache.shardingsphere.elasticjob.kernel.internal.instance;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.server.ServerService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ServerService serverService;
    
    private InstanceService instanceService;
    
    @BeforeEach
    void setUp() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0", null, "127.0.0.1"));
        instanceService = new InstanceService(null, "test_job");
        InstanceNode instanceNode = new InstanceNode("test_job");
        ReflectionUtils.setFieldValue(instanceService, "instanceNode", instanceNode);
        ReflectionUtils.setFieldValue(instanceService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(instanceService, "serverService", serverService);
    }
    
    @Test
    void assertPersistOnline() {
        instanceService.persistOnline();
        verify(jobNodeStorage).fillEphemeralJobNode("instances/127.0.0.1@-@0", "jobInstanceId: 127.0.0.1@-@0\nserverIp: 127.0.0.1\n");
    }
    
    @Test
    void assertRemoveInstance() {
        instanceService.removeInstance();
        verify(jobNodeStorage).removeJobNodeIfExisted("instances/127.0.0.1@-@0");
    }
    
    @Test
    void assertGetAvailableJobInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("127.0.0.1@-@0", "127.0.0.2@-@0"));
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nlabels: labels\nserverIp: 127.0.0.1\n");
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.2@-@0")).thenReturn("jobInstanceId: 127.0.0.2@-@0\nlabels: labels\nserverIp: 127.0.0.2\n");
        when(serverService.isEnableServer("127.0.0.1")).thenReturn(true);
        assertThat(instanceService.getAvailableJobInstances(), is(Collections.singletonList(new JobInstance("127.0.0.1@-@0"))));
    }
    
    @Test
    void assertGetAvailableJobInstancesWhenInstanceRemoving() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("127.0.0.1@-@0", "127.0.0.2@-@0"));
        when(jobNodeStorage.getJobNodeData("instances/127.0.0.1@-@0")).thenReturn("jobInstanceId: 127.0.0.1@-@0\nlabels: labels\nserverIp: 127.0.0.1\n");
        when(serverService.isEnableServer("127.0.0.1")).thenReturn(true);
        assertThat(instanceService.getAvailableJobInstances(), is(Collections.singletonList(new JobInstance("127.0.0.1@-@0"))));
    }
    
    @Test
    void assertIsLocalJobInstanceExisted() {
        when(jobNodeStorage.isJobNodeExisted("instances/127.0.0.1@-@0")).thenReturn(true);
        assertTrue(instanceService.isLocalJobInstanceExisted());
    }
    
    @Test
    void assertTriggerAllInstances() {
        when(jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)).thenReturn(Arrays.asList("127.0.0.1@-@0", "127.0.0.2@-@0"));
        instanceService.triggerAllInstances();
        verify(jobNodeStorage).createJobNodeIfNeeded("trigger/127.0.0.1@-@0");
        verify(jobNodeStorage).createJobNodeIfNeeded("trigger/127.0.0.2@-@0");
    }
}
