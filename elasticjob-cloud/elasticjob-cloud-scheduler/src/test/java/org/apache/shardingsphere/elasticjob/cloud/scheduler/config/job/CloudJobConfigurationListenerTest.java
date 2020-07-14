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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.config.job;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.shardingsphere.elasticjob.cloud.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.cloud.config.CloudJobExecutionType;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.fixture.CloudJsonConstants;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.producer.ProducerManager;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.state.ready.ReadyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class CloudJobConfigurationListenerTest {
    
    @Mock
    private ProducerManager producerManager;
    
    @Mock
    private ReadyService readyService;
    
    @InjectMocks
    private CloudJobConfigurationListener cloudJobConfigurationListener;
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "producerManager", producerManager);
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "readyService", readyService);
    }
    
    @Test
    public void assertChildEventWhenDataIsNull() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, null));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenIsNotConfigPath() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/other/test_job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenIsRootConfigPath() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/config/job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPathAndInvalidData() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/job/test_job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPath() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes())));
        verify(producerManager).schedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndTransientJob() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes())));
        verify(readyService, times(0)).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndDaemonJob() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, 
                new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON).getBytes())));
        verify(readyService).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndMisfireDisabled() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED,
                new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(false).getBytes())));
        verify(readyService).setMisfireDisabled("test_job");
        verify(producerManager).reschedule(ArgumentMatchers.any());
    }
    
    @Test
    public void assertChildEventWhenStateIsRemovedAndIsJobConfigPath() {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/config/job/test_job", null, "".getBytes())));
        verify(producerManager).unschedule("test_job");
    }
}
