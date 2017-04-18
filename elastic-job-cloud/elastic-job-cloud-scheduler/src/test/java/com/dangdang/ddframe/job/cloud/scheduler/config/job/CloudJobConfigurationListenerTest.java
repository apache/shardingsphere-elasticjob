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

package com.dangdang.ddframe.job.cloud.scheduler.config.job;

import com.dangdang.ddframe.job.cloud.scheduler.fixture.CloudJsonConstants;
import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.util.ReflectionUtils;

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
    public void setUp() throws NoSuchFieldException {
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "producerManager", producerManager);
        ReflectionUtils.setFieldValue(cloudJobConfigurationListener, "readyService", readyService);
    }
    
    @Test
    public void assertChildEventWhenDataIsNull() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, null));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenIsNotConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/other/test_job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenIsRootConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/config/job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPathAndInvalidData() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/job/test_job", null, "".getBytes())));
        verify(producerManager, times(0)).schedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
        verify(producerManager, times(0)).unschedule(ArgumentMatchers.<String>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsAddAndIsConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_ADDED, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes())));
        verify(producerManager).schedule(ArgumentMatchers.<CloudJobConfiguration>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndTransientJob() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson().getBytes())));
        verify(readyService, times(0)).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndDaemonJob() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED, 
                new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(CloudJobExecutionType.DAEMON).getBytes())));
        verify(readyService).remove(Collections.singletonList("test_job"));
        verify(producerManager).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsUpdateAndIsConfigPathAndMisfireDisabled() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED,
                new ChildData("/config/job/test_job", null, CloudJsonConstants.getJobJson(false).getBytes())));
        verify(readyService).setMisfireDisabled("test_job");
        verify(producerManager).reschedule(ArgumentMatchers.<CloudJobConfiguration>any());
    }
    
    @Test
    public void assertChildEventWhenStateIsRemovedAndIsJobConfigPath() throws Exception {
        cloudJobConfigurationListener.childEvent(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED, new ChildData("/config/job/test_job", null, "".getBytes())));
        verify(producerManager).unschedule("test_job");
    }
}
