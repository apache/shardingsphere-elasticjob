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

package com.dangdang.ddframe.job.api.type.dataflow.executor.throughput;

import com.dangdang.ddframe.job.api.type.dataflow.executor.AbstractDataflowJobExecutorTest;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StreamingThroughputDataflowJobTest extends AbstractDataflowJobExecutorTest {
    
    public StreamingThroughputDataflowJobTest() {
        super(DataflowJobConfiguration.DataflowType.THROUGHPUT, true, 10);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNotEligibleForJobRunning() {
        when(getJobCaller().fetchData(0)).thenReturn(Collections.<Object>singletonList(1));
        when(getJobCaller().fetchData(1)).thenReturn(null);
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(false);
        getDataflowJobExecutor().execute();
        verify(getJobCaller()).fetchData(0);
        verify(getJobCaller()).fetchData(1);
        verify(getJobCaller()).processData(any());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() {
        when(getJobCaller().fetchData(0)).thenReturn(Collections.<Object>singletonList(1), Collections.emptyList());
        when(getJobCaller().fetchData(1)).thenReturn(Collections.<Object>singletonList(2), Collections.emptyList());
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(true);
        getDataflowJobExecutor().execute();
        verify(getJobCaller(), times(2)).fetchData(0);
        verify(getJobCaller(), times(2)).fetchData(1);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndProcessFailureWithException() {
        when(getJobCaller().fetchData(0)).thenReturn(Collections.<Object>singletonList(1), Collections.emptyList());
        when(getJobCaller().fetchData(1)).thenReturn(Arrays.<Object>asList(2, 3), Collections.emptyList());
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(true);
        doThrow(new IllegalStateException()).when(getJobCaller()).processData(2);
        getDataflowJobExecutor().execute();
        verify(getJobCaller(), times(2)).fetchData(0);
        verify(getJobCaller(), times(2)).fetchData(1);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller(), times(0)).processData(3);
    }
}
