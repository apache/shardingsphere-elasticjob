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

package org.apache.shardingsphere.elasticjob.lite.internal.guarantee;

import org.apache.curator.framework.recipes.cache.CuratorCacheListener.Type;
import org.apache.shardingsphere.elasticjob.lite.api.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.api.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.listener.AbstractJobListener;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class GuaranteeListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ElasticJobListener elasticJobListener;
    
    @Mock
    private AbstractDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    private GuaranteeListenerManager guaranteeListenerManager;
    
    @Before
    public void setUp() {
        guaranteeListenerManager = new GuaranteeListenerManager(null, "test_job", Arrays.asList(elasticJobListener, distributeOnceElasticJobListener));
        ReflectionUtils.setSuperclassFieldValue(guaranteeListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertStart() {
        guaranteeListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(ArgumentMatchers.<AbstractJobListener>any());
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged("/test_job/guarantee/started", Type.NODE_CHANGED, "");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsNotStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged("/other_job/guarantee/started", Type.NODE_DELETED, "");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertStartedNodeRemovedJobListenerWhenIsRemovedAndStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().dataChanged("/test_job/guarantee/started", Type.NODE_DELETED, "");
        verify(distributeOnceElasticJobListener).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged("/test_job/guarantee/completed", Type.NODE_CHANGED, "");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsNotCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged("/other_job/guarantee/completed", Type.NODE_DELETED, "");
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    public void assertCompletedNodeRemovedJobListenerWhenIsRemovedAndCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().dataChanged("/test_job/guarantee/completed", Type.NODE_DELETED, "");
        verify(distributeOnceElasticJobListener).notifyWaitingTaskComplete();
    }
}
