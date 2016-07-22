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

package com.dangdang.ddframe.job.api.type.dataflow.throughput;

import com.dangdang.ddframe.job.api.job.dataflow.AbstractDataflowElasticJob;
import com.dangdang.ddframe.job.api.job.dataflow.DataflowType;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.dataflow.AbstractDataflowElasticJobTest;
import com.dangdang.ddframe.job.api.type.fixture.FooStreamingThroughputDataflowElasticJob;
import com.dangdang.ddframe.job.api.type.fixture.JobCaller;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingThroughputDataflowElasticJobTest extends AbstractDataflowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNull() {
        when(getJobCaller().fetchData()).thenReturn(null);
        getDataflowElasticJob().execute();
        verify(getJobCaller()).fetchData();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsEmpty() {
        when(getJobCaller().fetchData()).thenReturn(Collections.emptyList());
        getDataflowElasticJob().execute();
        verify(getJobCaller()).fetchData();
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNotEligibleForJobRunning() {
        when(getJobCaller().fetchData()).thenReturn(Collections.<Object>singletonList(1));
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(false);
        getDataflowElasticJob().execute();
        verify(getJobCaller()).fetchData();
        verify(getJobCaller()).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 0);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() {
        when(getJobCaller().fetchData()).thenReturn(Arrays.<Object>asList(1, 2, 3), Collections.emptyList());
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(true);
        doThrow(new IllegalStateException()).when(getJobCaller()).processData(3);
        getDataflowElasticJob().execute();
        verify(getJobCaller(), times(2)).fetchData();
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 1);
    }
    
    @Override
    protected DataflowType getDataflowType() {
        return DataflowType.THROUGHPUT;
    }
    
    @Override
    protected boolean isStreamingProcess() {
        return true;
    }
    
    @Override
    protected AbstractDataflowElasticJob createDataflowElasticJob(final JobCaller jobCaller) {
        return new FooStreamingThroughputDataflowElasticJob(jobCaller);
    }
}
