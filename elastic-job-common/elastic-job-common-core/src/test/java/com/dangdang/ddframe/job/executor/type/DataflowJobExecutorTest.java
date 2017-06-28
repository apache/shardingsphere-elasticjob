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

package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.fixture.ShardingContextsBuilder;
import com.dangdang.ddframe.job.fixture.config.TestDataflowJobConfiguration;
import com.dangdang.ddframe.job.fixture.job.JobCaller;
import com.dangdang.ddframe.job.fixture.job.TestDataflowJob;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataflowJobExecutorTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private ShardingContexts shardingContexts;
    
    private DataflowJobExecutor dataflowJobExecutor;
    
    @After
    public void tearDown() throws NoSuchFieldException {
        verify(jobFacade).loadJobRootConfiguration(true);
        ElasticJobVerify.verifyForIsNotMisfire(jobFacade, shardingContexts);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNullAndEmpty() {
        setUp(true, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobCaller.fetchData(0)).thenReturn(null);
        when(jobCaller.fetchData(1)).thenReturn(Collections.emptyList());
        dataflowJobExecutor.execute();
        verify(jobCaller).fetchData(0);
        verify(jobCaller).fetchData(1);
        verify(jobCaller, times(0)).processData(any());
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForUnStreamingProcessAndSingleShardingItem() {
        setUp(false, ShardingContextsBuilder.getSingleShardingContexts());
        doThrow(new IllegalStateException()).when(jobCaller).fetchData(0);
        dataflowJobExecutor.execute();
        verify(jobCaller).fetchData(0);
        verify(jobCaller, times(0)).processData(any());
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForUnStreamingProcessAndMultipleShardingItems() {
        setUp(false, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobCaller.fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2));
        when(jobCaller.fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4));
        doThrow(new IllegalStateException()).when(jobCaller).processData(4);
        dataflowJobExecutor.execute();
        verify(jobCaller).fetchData(0);
        verify(jobCaller).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        verify(jobCaller).processData(3);
        verify(jobCaller).processData(4);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForStreamingProcessAndSingleShardingItem() {
        setUp(true, ShardingContextsBuilder.getSingleShardingContexts());
        when(jobCaller.fetchData(0)).thenReturn(Collections.<Object>singletonList(1), Collections.emptyList());
        when(jobFacade.isEligibleForJobRunning()).thenReturn(true);
        dataflowJobExecutor.execute();
        verify(jobCaller, times(2)).fetchData(0);
        verify(jobCaller).processData(1);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForStreamingProcessAndMultipleShardingItems() {
        setUp(true, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobCaller.fetchData(0)).thenReturn(Collections.<Object>singletonList(1), Collections.emptyList());
        when(jobCaller.fetchData(1)).thenReturn(Collections.<Object>singletonList(2), Collections.emptyList());
        when(jobFacade.isEligibleForJobRunning()).thenReturn(true);
        dataflowJobExecutor.execute();
        verify(jobCaller, times(2)).fetchData(0);
        verify(jobCaller, times(2)).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndProcessFailureWithExceptionForStreamingProcess() {
        setUp(true, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobCaller.fetchData(0)).thenReturn(Collections.<Object>singletonList(1), Collections.emptyList());
        when(jobCaller.fetchData(1)).thenReturn(Arrays.<Object>asList(2, 3), Collections.emptyList());
        when(jobFacade.isEligibleForJobRunning()).thenReturn(true);
        doThrow(new IllegalStateException()).when(jobCaller).processData(2);
        dataflowJobExecutor.execute();
        verify(jobCaller, times(2)).fetchData(0);
        verify(jobCaller, times(1)).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        verify(jobCaller, times(0)).processData(3);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsEligibleForJobRunningForStreamingProcess() {
        setUp(true, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobFacade.isEligibleForJobRunning()).thenReturn(true);
        when(jobCaller.fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2), Collections.emptyList());
        when(jobCaller.fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4), Collections.emptyList());
        doThrow(new IllegalStateException()).when(jobCaller).processData(4);
        dataflowJobExecutor.execute();
        verify(jobCaller, times(2)).fetchData(0);
        verify(jobCaller, times(1)).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        verify(jobCaller).processData(3);
        verify(jobCaller).processData(4);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNotEligibleForJobRunningForStreamingProcess() {
        setUp(true, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobFacade.isEligibleForJobRunning()).thenReturn(false);
        when(jobCaller.fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2));
        when(jobCaller.fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4));
        doThrow(new IllegalStateException()).when(jobCaller).processData(4);
        dataflowJobExecutor.execute();
        verify(jobCaller).fetchData(0);
        verify(jobCaller).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        verify(jobCaller).processData(3);
        verify(jobCaller).processData(4);
    }
    
    private void setUp(final boolean isStreamingProcess, final ShardingContexts shardingContexts) {
        this.shardingContexts = shardingContexts;
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(isStreamingProcess));
        when(jobFacade.getShardingContexts()).thenReturn(shardingContexts);
        dataflowJobExecutor = new DataflowJobExecutor(new TestDataflowJob(jobCaller), jobFacade);
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
    }
}
