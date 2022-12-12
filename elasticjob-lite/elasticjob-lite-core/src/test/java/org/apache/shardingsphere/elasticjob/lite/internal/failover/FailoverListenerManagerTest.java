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

package org.apache.shardingsphere.elasticjob.lite.internal.failover;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.lite.internal.instance.InstanceService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobScheduleController;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ExecutionService;
import org.apache.shardingsphere.elasticjob.lite.internal.sharding.ShardingService;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FailoverListenerManagerTest {
    
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
    
    @Before
    public void setUp() {
        ReflectionUtils.setSuperclassFieldValue(failoverListenerManager, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(failoverListenerManager, "configService", configService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceService", instanceService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "executionService", executionService);
        ReflectionUtils.setFieldValue(failoverListenerManager, "instanceNode", instanceNode);
    }
    
    @Test
    public void assertStart() {
        failoverListenerManager.start();
        verify(jobNodeStorage, times(3)).addDataListener(ArgumentMatchers.any(DataChangedEventListener.class));
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenFailoverDisabled() {
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsNotNodeRemoved() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsNotInstancesPath() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/other/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsSameInstance() {
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
        JobRegistry.getInstance().registerJob("test_job", jobScheduleController);
        when(configService.load(true)).thenReturn(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").failover(true).build());
        failoverListenerManager.new JobCrashedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/instances/127.0.0.1@-@0", ""));
        verify(failoverService, times(0)).failoverIfNecessary();
        JobRegistry.getInstance().shutdown("test_job");
    }
    
    @Test
    public void assertJobCrashedJobListenerWhenIsOtherInstanceCrashed() {
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
    public void assertJobCrashedJobListenerWhenIsOtherFailoverInstanceCrashed() {
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
    public void assertFailoverSettingsChangedJobListenerWhenIsNotFailoverPath() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/other", LiteYamlConstants.getJobYaml()));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathButNotUpdate() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.ADDED, "/test_job/config", ""));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButEnableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYaml()));
        verify(failoverService, times(0)).removeFailoverInfo();
    }
    
    @Test
    public void assertFailoverSettingsChangedJobListenerWhenIsFailoverPathAndUpdateButDisableFailover() {
        failoverListenerManager.new FailoverSettingsChangedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/config", LiteYamlConstants.getJobYamlWithFailover(false)));
        verify(failoverService).removeFailoverInfo();
    }
    
    @Test
    public void assertLegacyCrashedRunningItemListenerWhenRunningItemsArePresent() {
        JobInstance jobInstance = new JobInstance("127.0.0.1@-@1");
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
    public void assertLegacyCrashedRunningItemListenerWhenJobInstanceAbsent() {
        failoverListenerManager.new LegacyCrashedRunningItemListener().onChange(new DataChangedEvent(Type.ADDED, "", ""));
        verifyNoInteractions(instanceNode);
    }
}
