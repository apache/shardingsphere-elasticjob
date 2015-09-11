/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.execution;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;

public final class ExecutionContextServiceTest extends AbstractBaseJobTest {
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final ExecutionContextService executionContextService = new ExecutionContextService(getRegistryCenter(), getJobConfig());
    
    @Test
    public void assertGetJobExecutionShardingContextWhenNotAssignShardingItem() {
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/config/jobParameter", "para");
        getRegistryCenter().persist("/testJob/config/fetchDataCount", "100");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.FALSE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "");
        JobExecutionMultipleShardingContext actual = executionContextService.getJobExecutionShardingContext();
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getJobParameter(), is("para"));
        assertThat(actual.getFetchDataCount(), is(100));
        assertTrue(actual.getShardingItems().isEmpty());
        assertFalse(actual.isMonitorExecution());
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenAssignShardingItems() {
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "0=A,2=C");
        getRegistryCenter().persist("/testJob/config/jobParameter", "para");
        getRegistryCenter().persist("/testJob/config/fetchDataCount", "100");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0,1");
        getRegistryCenter().persist("/testJob/execution/0", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        JobExecutionMultipleShardingContext actual = executionContextService.getJobExecutionShardingContext();
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItems(), is(Arrays.asList(0, 1)));
        Map<Integer, String> expectedRunningItemParameters = new HashMap<Integer, String>(1);
        expectedRunningItemParameters.put(0, "A");
        assertThat(actual.getShardingItemParameters(), is(expectedRunningItemParameters));
        assertThat(actual.getJobParameter(), is("para"));
        assertThat(actual.getFetchDataCount(), is(100));
        assertTrue(actual.isMonitorExecution());
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenEnableFailover() {
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "0=A,2=C");
        getRegistryCenter().persist("/testJob/config/jobParameter", "para");
        getRegistryCenter().persist("/testJob/config/fetchDataCount", "100");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0,1");
        getRegistryCenter().persist("/testJob/execution/2/failover", localHostService.getIp());
        getRegistryCenter().persist("/testJob/execution/0", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        JobExecutionMultipleShardingContext actual = executionContextService.getJobExecutionShardingContext();
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItems(), is(Arrays.asList(2)));
        Map<Integer, String> expectedRunningItemParameters = new HashMap<Integer, String>(1);
        expectedRunningItemParameters.put(2, "C");
        assertThat(actual.getShardingItemParameters(), is(expectedRunningItemParameters));
        assertThat(actual.getJobParameter(), is("para"));
        assertThat(actual.getFetchDataCount(), is(100));
        assertTrue(actual.isMonitorExecution());
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenEnableFailoverAndForTakeOff() {
        getRegistryCenter().persist("/testJob/config/failover", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "0=A,2=C");
        getRegistryCenter().persist("/testJob/config/jobParameter", "para");
        getRegistryCenter().persist("/testJob/config/fetchDataCount", "100");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0,1");
        getRegistryCenter().persist("/testJob/execution/0/failover", "host0");
        getRegistryCenter().persist("/testJob/execution/0", "");
        getRegistryCenter().persist("/testJob/execution/1/completed", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        JobExecutionMultipleShardingContext actual = executionContextService.getJobExecutionShardingContext();
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItems(), is(Arrays.asList(1)));
        assertThat(actual.getShardingItemParameters(), is(Collections.EMPTY_MAP));
        assertThat(actual.getJobParameter(), is("para"));
        assertThat(actual.getFetchDataCount(), is(100));
        assertTrue(actual.isMonitorExecution());
    }
    
    @Test
    public void assertGetJobExecutionShardingContextWhenHasRunningItems() {
        getRegistryCenter().persist("/testJob/config/shardingTotalCount", "3");
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "0=A,2=C");
        getRegistryCenter().persist("/testJob/config/jobParameter", "para");
        getRegistryCenter().persist("/testJob/config/fetchDataCount", "100");
        getRegistryCenter().persist("/testJob/config/monitorExecution", Boolean.TRUE.toString());
        getRegistryCenter().persist("/testJob/servers/" + localHostService.getIp() + "/sharding", "0,1");
        getRegistryCenter().persist("/testJob/execution/0/running", "");
        getRegistryCenter().persist("/testJob/execution/1", "");
        getRegistryCenter().persist("/testJob/execution/2", "");
        JobExecutionMultipleShardingContext actual = executionContextService.getJobExecutionShardingContext();
        assertThat(actual.getJobName(), is("testJob"));
        assertThat(actual.getShardingTotalCount(), is(3));
        assertThat(actual.getShardingItems(), is(Arrays.asList(1)));
        assertThat(actual.getShardingItemParameters(), is(Collections.EMPTY_MAP));
        assertThat(actual.getJobParameter(), is("para"));
        assertThat(actual.getFetchDataCount(), is(100));
        assertTrue(actual.isMonitorExecution());
    }
}
