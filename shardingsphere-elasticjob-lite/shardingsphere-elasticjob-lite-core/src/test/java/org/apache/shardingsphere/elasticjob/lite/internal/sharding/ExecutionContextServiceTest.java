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

package org.apache.shardingsphere.elasticjob.lite.internal.sharding;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.internal.config.ConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ExecutionContextServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    @Mock
    private ConfigurationService configService;
    
    private final ExecutionContextService executionContextService = new ExecutionContextService(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(executionContextService, "jobNodeStorage", jobNodeStorage);
        ReflectionUtils.setFieldValue(executionContextService, "configService", configService);
        JobRegistry.getInstance().addJobInstance("test_job", new JobInstance("127.0.0.1@-@0"));
    }
    
    @Test
    public void assertGetShardingContextWhenNotAssignShardingItem() {
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").setProperty("streaming.process", Boolean.TRUE.toString()).monitorExecution(false).build());
        ShardingContexts shardingContexts = executionContextService.getJobShardingContext(Collections.emptyList());
        assertTrue(shardingContexts.getTaskId().startsWith("test_job@-@@-@READY@-@"));
        assertThat(shardingContexts.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertGetShardingContextWhenAssignShardingItems() {
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").setProperty("streaming.process", Boolean.TRUE.toString()).monitorExecution(false).build());
        Map<Integer, String> map = new HashMap<>(3);
        map.put(0, "A");
        map.put(1, "B");
        ShardingContexts expected = new ShardingContexts("fake_task_id", "test_job", 3, "", map);
        assertShardingContext(executionContextService.getJobShardingContext(Arrays.asList(0, 1)), expected);
    }
    
    @Test
    public void assertGetShardingContextWhenHasRunningItems() {
        when(configService.load(false)).thenReturn(JobConfiguration.newBuilder("test_job", 3)
                .cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").setProperty("streaming.process", Boolean.TRUE.toString()).monitorExecution(true).build());
        when(jobNodeStorage.isJobNodeExisted("sharding/0/running")).thenReturn(false);
        when(jobNodeStorage.isJobNodeExisted("sharding/1/running")).thenReturn(true);
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        ShardingContexts expected = new ShardingContexts("fake_task_id", "test_job", 3, "", map);
        assertShardingContext(executionContextService.getJobShardingContext(Lists.newArrayList(0, 1)), expected);
    }
    
    private void assertShardingContext(final ShardingContexts actual, final ShardingContexts expected) {
        assertThat(actual.getJobName(), is(expected.getJobName()));
        assertThat(actual.getShardingTotalCount(), is(expected.getShardingTotalCount()));
        assertThat(actual.getJobParameter(), is(expected.getJobParameter()));
        assertThat(actual.getShardingItemParameters().size(), is(expected.getShardingItemParameters().size()));
        for (int i = 0; i < expected.getShardingItemParameters().size(); i++) {
            assertThat(actual.getShardingItemParameters().get(i), is(expected.getShardingItemParameters().get(i)));
        }
    }
}
