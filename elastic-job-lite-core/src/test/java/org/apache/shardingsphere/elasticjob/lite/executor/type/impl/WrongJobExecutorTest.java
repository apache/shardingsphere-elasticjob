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

package org.apache.shardingsphere.elasticjob.lite.executor.type.impl;

import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent.State;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.fixture.config.TestSimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.job.TestWrongJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class WrongJobExecutorTest {
    
    @Mock
    private JobFacade jobFacade;
    
    private ElasticJobExecutor wrongJobExecutor;
    
    @Before
    public void setUp() {
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestSimpleJobConfiguration(null, "THROW"));
        wrongJobExecutor = new ElasticJobExecutor(new TestWrongJob(), jobFacade, new SimpleJobExecutor());
    }
    
    @Test(expected = RuntimeException.class)
    public void assertWrongJobExecutorWithSingleItem() {
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", map);
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        wrongJobExecutor.execute();
    }
    
    @Test
    public void assertWrongJobExecutorWithMultipleItems() {
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        map.put(1, "B");
        ShardingContexts shardingContexts = new ShardingContexts("fake_task_id", "test_job", 10, "", map);
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        wrongJobExecutor.execute();
        verify(jobFacade).getShardingContexts();
        verify(jobFacade).postJobStatusTraceEvent("fake_task_id", State.TASK_RUNNING, "");
    }
}
