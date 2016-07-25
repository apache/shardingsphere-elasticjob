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

package com.dangdang.ddframe.job.api.type.dataflow.sequence;

import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.fixture.FooStreamingSequenceDataflowElasticJob;
import com.dangdang.ddframe.job.api.type.fixture.JobCaller;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingSequenceDataflowElasticJobTest extends AbstractSequenceDataflowElasticJobTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsEligibleForJobRunning() {
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(true);
        when(getJobCaller().fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2), Collections.emptyList());
        when(getJobCaller().fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4), Collections.emptyList());
        doThrow(new IllegalStateException()).when(getJobCaller()).processData(4);
        getDataflowElasticJobExecutor().execute();
        verify(getJobCaller(), times(2)).fetchData(0);
        verify(getJobCaller(), times(2)).fetchData(1);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller()).processData(3);
        verify(getJobCaller()).processData(4);
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 1);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNotEligibleForJobRunning() {
        when(getJobFacade().isEligibleForJobRunning()).thenReturn(false);
        when(getJobCaller().fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobCaller().fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4));
        doThrow(new IllegalStateException()).when(getJobCaller()).processData(4);
        getDataflowElasticJobExecutor().execute();
        verify(getJobCaller()).fetchData(0);
        verify(getJobCaller()).fetchData(1);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller()).processData(3);
        verify(getJobCaller()).processData(4);
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(1, 1);
    }
    
    @Override
    protected DataflowType getDataflowType() {
        return DataflowType.SEQUENCE;
    }
    
    @Override
    protected boolean isStreamingProcess() {
        return true;
    }
    
    @Override
    protected DataflowElasticJob createDataflowElasticJob(final JobCaller jobCaller) {
        return new FooStreamingSequenceDataflowElasticJob(jobCaller);
    }
}
