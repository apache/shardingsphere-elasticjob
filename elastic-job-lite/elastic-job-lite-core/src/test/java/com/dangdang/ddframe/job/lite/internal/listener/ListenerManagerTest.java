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

package com.dangdang.ddframe.job.lite.internal.listener;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationListenerManager;
import com.dangdang.ddframe.job.lite.internal.election.ElectionListenerManager;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionListenerManager;
import com.dangdang.ddframe.job.lite.internal.failover.FailoverListenerManager;
import com.dangdang.ddframe.job.lite.internal.guarantee.GuaranteeListenerManager;
import com.dangdang.ddframe.job.lite.internal.server.JobOperationListenerManager;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingListenerManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.mockito.Mockito.verify;

public class ListenerManagerTest {
    
    @Mock
    private ElectionListenerManager electionListenerManager;
    
    @Mock
    private ShardingListenerManager shardingListenerManager;
    
    @Mock
    private ExecutionListenerManager executionListenerManager;
    
    @Mock
    private FailoverListenerManager failoverListenerManager;
    
    @Mock
    private JobOperationListenerManager jobOperationListenerManager;
    
    @Mock
    private ConfigurationListenerManager configurationListenerManager;
    
    @Mock
    private GuaranteeListenerManager guaranteeListenerManager;
    
    private final ListenerManager listenerManager = new ListenerManager(null, "test_job", Collections.<ElasticJobListener>emptyList());
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(listenerManager, "electionListenerManager", electionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "shardingListenerManager", shardingListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "executionListenerManager", executionListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "failoverListenerManager", failoverListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "jobOperationListenerManager", jobOperationListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "configurationListenerManager", configurationListenerManager);
        ReflectionUtils.setFieldValue(listenerManager, "guaranteeListenerManager", guaranteeListenerManager);
    }
    
    @Test
    public void assertStartAllListeners() {
        listenerManager.startAllListeners();
        verify(electionListenerManager).start();
        verify(shardingListenerManager).start();
        verify(executionListenerManager).start();
        verify(failoverListenerManager).start();
        verify(jobOperationListenerManager).start();
        verify(configurationListenerManager).start();
        verify(guaranteeListenerManager).start();
    }
    
    @Test
    public void assertSetCurrentShardingTotalCount() {
        listenerManager.setCurrentShardingTotalCount(10);
        verify(shardingListenerManager).setCurrentShardingTotalCount(10);
    }
}
