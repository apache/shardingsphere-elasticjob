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

package com.dangdang.ddframe.job.lite.internal.guarantee;

import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public final class GuaranteeListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ElasticJobListener elasticJobListener;
    
    @Mock
    private AbstractDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    private GuaranteeListenerManager guaranteeListenerManager;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        guaranteeListenerManager = new GuaranteeListenerManager(null, "test_job", Arrays.asList(elasticJobListener, distributeOnceElasticJobListener));
        ReflectionUtils.setFieldValue(guaranteeListenerManager, guaranteeListenerManager.getClass().getSuperclass().getDeclaredField("jobNodeStorage"), jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        guaranteeListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(Matchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED,
                new ChildData("/test_job/guarantee/started", null, "".getBytes())), "/test_job/guarantee/started");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED,
                new ChildData("/other_job/guarantee/started", null, "".getBytes())), "/other_job/guarantee/started");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsRemovedAndStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED,
                new ChildData("/test_job/guarantee/started", null, "".getBytes())), "/test_job/guarantee/started");
        verify(distributeOnceElasticJobListener).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_UPDATED,
                new ChildData("/test_job/guarantee/completed", null, "".getBytes())), "/test_job/guarantee/completed");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED,
                new ChildData("/other_job/guarantee/completed", null, "".getBytes())), "/other_job/guarantee/completed");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsRemovedAndCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged(null, new TreeCacheEvent(TreeCacheEvent.Type.NODE_REMOVED,
                new ChildData("/test_job/guarantee/completed", null, "".getBytes())), "/test_job/guarantee/completed");
        verify(distributeOnceElasticJobListener).notifyWaitingTaskComplete();
    }
}
