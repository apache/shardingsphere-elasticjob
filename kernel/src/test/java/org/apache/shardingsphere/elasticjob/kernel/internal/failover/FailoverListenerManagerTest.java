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

package org.apache.shardingsphere.elasticjob.kernel.internal.failover;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.fixture.YamlConstants;
import org.apache.shardingsphere.elasticjob.kernel.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.kernel.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.kernel.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.kernel.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailoverListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private JobScheduleController jobScheduleController;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private InstanceService instanceService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private InstanceNode instanceNode;
    
    private final FailoverListenerManager failoverListenerManager = new FailoverListenerManager(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setSuperclassFieldValue(failoverListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverListenerManager, "configService", configService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceNode", instanceNode);
    }
    
    @Test
    void assertStart() {
        failoverListenerManager.start();
        verify(jobNodeStorage, times(3)).addDataListener(ArgumentMatchers.any(DataChangedEventListener.class));
    }
    
    @Test
    void assertJobCrashedJobListenerWhenFailoverDisabled() {
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    void assertJobCrashedJobListenerWhenIsNotNodeRemoved() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertJobCrashedJobListenerWhenIsNotInstancesPath() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/other/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertJobCrashedJobListenerWhenIsSameInstance() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertJobCrashedJobListenerWhenIsOtherInstanceCrashed() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        when(shardingService.getCrashedShardingItems("127.0.0.1@-@1")).thenReturn(Arrays.asList(0, 2));
        when(instanceNode.isInstancePath("/test_job/instances/127.0.0.1@-@1")).thenReturn(true);
        when(instanceNode.getInstanceFullPath()).thenReturn("/test_job/instances");
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@1", ""));
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(failoverService).setCrashedFailoverFlag(2);
        verify(failoverService, times(2)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertJobCrashedJobListenerWhenIsOtherFailoverInstanceCrashed() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        when(failoverService.getFailoveringItems("127.0.0.1@-@1")).thenReturn(Collections.singletonList(1));
        when(instanceNode.isInstancePath("/test_job/instances/127.0.0.1@-@1")).thenReturn(true);
        when(instanceNode.getInstanceFullPath()).thenReturn("/test_job/instances");
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@1", ""));
        verify(failoverService).setCrashedFailoverFlagDirectly(1);
        verify(failoverService).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    void assertFailoverSettingsChangedJobListenerWhenIsNotFailoverPath() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/other", YamlConstants.getJobYaml()));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/config", ""));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", YamlConstants.getJobYaml()));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", YamlConstants.getJobYamlWithFailover(false)));
        verify(failoverService).removeFailoverInfo();
    }
    
    @Test
    void assertLegacyCrashedRunningItemListenerWhenRunningItemsArePresent() {
        JobInstance jobInstance = new JobInstance("127.0.0.1@-@1");
        JobRegistry.getInstance().registerJob("test_job", mock(JobScheduleController.class));
        JobRegistry.getInstance().addJobInstance("test_job", jobInstance);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        when(instanceNode.getLocalInstancePath()).thenReturn("instances/127.0.0.1@-@1");
        when(instanceService.getAvailableJobInstances()).thenReturn(Collections.singletonList(jobInstance));
        Map<Integer, JobInstance> allRunningItems = new LinkedHashMap<>(2, 1);
        allRunningItems.put(0, new JobInstance("127.0.0.1@-@2"));
        allRunningItems.put(1, new JobInstance("127.0.0.1@-@2"));
        when(executionService.getAllRunningItems()).thenReturn(allRunningItems);
        when(failoverService.getAllFailoveringItems()).thenReturn(Collections.singletonMap(1, new JobInstance("127.0.0.1@-@2")));
        failoverListenerManager.new LegacyCrashedRunningItemListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/instances/127.0.0.1@-@1", ""));
        verify(failoverService).setCrashedFailoverFlagDirectly(1);
        verify(failoverService).clearFailoveringItem(1);
        verify(executionService).clearRunningInfo(Collections.singletonList(1));
        verify(failoverService).setCrashedFailoverFlag(0);
        verify(executionService).clearRunningInfo(Collections.singletonList(0));
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    void assertLegacyCrashedRunningItemListenerWhenJobInstanceAbsent() {
        failoverListenerManager.new LegacyCrashedRunningItemListener().onChange(new DataChangedEvent(Type.ADDED, "", ""));
        verifyNoInteractions(instanceNode);
    }
}
