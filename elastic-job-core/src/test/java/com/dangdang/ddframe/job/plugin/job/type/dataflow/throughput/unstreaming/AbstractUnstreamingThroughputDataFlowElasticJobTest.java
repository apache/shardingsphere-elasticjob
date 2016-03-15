/*
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

package com.dangdang.ddframe.job.plugin.job.type.dataflow.throughput.unstreaming;

import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractDataFlowElasticJobTest;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;
import org.quartz.JobExecutionException;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractUnstreamingThroughputDataFlowElasticJobTest extends AbstractDataFlowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNull() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(null);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsEmpty() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Collections.emptyList());
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOne() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobCaller().processData(1)).thenReturn(true);
        when(getJobCaller().processData(2)).thenReturn(true);
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(2, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndDataIsOne() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Collections.<Object>singletonList(1));
        when(getJobCaller().processData(1)).thenReturn(true);
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(2);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).processData(1);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOneAndProcessFailureWithException() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        doThrow(NullPointerException.class).when(getJobCaller()).processData(any());
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 2);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndConcurrentDataProcessThreadCountIsOneAndProcessFailureWithWrongResult() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobCaller().processData(1)).thenReturn(true, true);
        when(getJobCaller().processData(2)).thenReturn(false, false);
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(1);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 1);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyForMultipleThread() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2, 3, 4));
        when(getJobCaller().processData(1)).thenReturn(true);
        when(getJobCaller().processData(2)).thenReturn(true);
        when(getJobCaller().processData(3)).thenReturn(true);
        when(getJobCaller().processData(4)).thenReturn(true);
        when(getJobFacade().getConcurrentDataProcessThreadCount()).thenReturn(2);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller()).processData(3);
        verify(getJobCaller()).processData(4);
        verify(getJobFacade()).getConcurrentDataProcessThreadCount();
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(4, 0);
    }
    
    @Test
    public void assertUpdateOffset() {
        getDataFlowElasticJob().updateOffset(0, "offset1");
        verify(getJobFacade()).updateOffset(0, "offset1");
    }
}
