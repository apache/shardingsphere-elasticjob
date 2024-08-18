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

package org.apache.shardingsphere.elasticjob.kernel.internal.guarantee;

import org.apache.shardingsphere.elasticjob.spi.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.listener.AbstractDistributeOnceElasticJobListener;
import org.apache.shardingsphere.elasticjob.kernel.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.test.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.elasticjob.reg.listener.DataChangedEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GuaranteeListenerManagerTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ElasticJobListener elasticJobListener;
    
    @Mock
    private AbstractDistributeOnceElasticJobListener distributeOnceElasticJobListener;
    
    private GuaranteeListenerManager guaranteeListenerManager;
    
    @BeforeEach
    void setUp() {
        guaranteeListenerManager = new GuaranteeListenerManager(null, "test_job", Arrays.asList(elasticJobListener, distributeOnceElasticJobListener));
        ReflectionUtils.setSuperclassFieldValue(guaranteeListenerManager, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    void assertStart() {
        guaranteeListenerManager.start();
        verify(jobNodeStorage, times(2)).addDataListener(any(DataChangedEventListener.class));
    }
    
    @Test
    void assertStartedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(DataChangedEvent.Type.UPDATED, "/test_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    void assertStartedNodeRemovedJobListenerWhenIsNotStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/other_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    void assertStartedNodeRemovedJobListenerWhenIsRemovedAndStartedNode() {
        guaranteeListenerManager.new StartedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/guarantee/started", ""));
        verify(distributeOnceElasticJobListener).notifyWaitingTaskStart();
    }
    
    @Test
    void assertCompletedNodeRemovedJobListenerWhenIsNotRemoved() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.UPDATED, "/test_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    void assertCompletedNodeRemovedJobListenerWhenIsNotCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/other_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener, times(0)).notifyWaitingTaskStart();
    }
    
    @Test
    void assertCompletedNodeRemovedJobListenerWhenIsRemovedAndCompletedNode() {
        guaranteeListenerManager.new CompletedNodeRemovedJobListener().onChange(new DataChangedEvent(Type.DELETED, "/test_job/guarantee/completed", ""));
        verify(distributeOnceElasticJobListener).notifyWaitingTaskComplete();
    }
}
