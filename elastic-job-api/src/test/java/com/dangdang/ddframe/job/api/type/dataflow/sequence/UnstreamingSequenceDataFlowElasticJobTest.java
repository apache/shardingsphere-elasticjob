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

import com.dangdang.ddframe.job.api.job.dataflow.AbstractDataFlowElasticJob;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.dataflow.sequence.AbstractSequenceDataFlowElasticJobTest;
import com.dangdang.ddframe.job.api.type.fixture.FooUnstreamingSequenceDataFlowElasticJob;
import com.dangdang.ddframe.job.api.type.fixture.JobCaller;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public class UnstreamingSequenceDataFlowElasticJobTest extends AbstractSequenceDataFlowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() {
        when(getJobCaller().fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2));
        when(getJobCaller().fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4));
        doThrow(new IllegalStateException()).when(getJobCaller()).processData(4);
        getDataFlowElasticJob().execute();
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
    protected boolean isStreamingProcess() {
        return false;
    }
    
    @Override
    protected AbstractDataFlowElasticJob createDataFlowElasticJob(final JobCaller jobCaller) {
        return new FooUnstreamingSequenceDataFlowElasticJob(jobCaller);
    }
}
