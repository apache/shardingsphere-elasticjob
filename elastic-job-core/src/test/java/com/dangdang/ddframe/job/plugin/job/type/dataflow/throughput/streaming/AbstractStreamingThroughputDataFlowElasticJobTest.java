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

package com.dangdang.ddframe.job.plugin.job.type.dataflow.throughput.streaming;

import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractDataFlowElasticJobTest;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;
import org.quartz.JobExecutionException;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractStreamingThroughputDataFlowElasticJobTest extends AbstractDataFlowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNull() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(null);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).fetchData();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsEmpty() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Collections.emptyList());
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).fetchData();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNotEligibleForJobRunning() throws JobExecutionException {
        when(getJobCaller().fetchData()).thenReturn(Collections.<Object>singletonList(1));
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(false);
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller()).fetchData();
        verify(getJobCaller()).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 1);
    }
}
