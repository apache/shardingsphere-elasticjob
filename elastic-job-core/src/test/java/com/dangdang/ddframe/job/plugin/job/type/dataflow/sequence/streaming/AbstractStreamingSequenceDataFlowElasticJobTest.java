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

package com.dangdang.ddframe.job.plugin.job.type.dataflow.sequence.streaming;

import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.sequence.AbstractSequenceDataFlowElasticJobTest;
import org.junit.Test;
import org.quartz.JobExecutionException;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class AbstractStreamingSequenceDataFlowElasticJobTest extends AbstractSequenceDataFlowElasticJobTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() throws JobExecutionException {
        when(getJobFacade().isEligibleForJobRunning(false)).thenReturn(true);
        when(getJobCaller().fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2), Collections.emptyList());
        when(getJobCaller().fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4), Collections.emptyList());
        when(getJobCaller().processData(1)).thenReturn(true);
        when(getJobCaller().processData(2)).thenReturn(true);
        when(getJobCaller().processData(3)).thenReturn(false);
        when(getJobCaller().processData(4)).thenThrow(new NullPointerException());
        getDataFlowElasticJob().execute(null);
        verify(getJobCaller(), times(2)).fetchData(0);
        verify(getJobCaller(), times(2)).fetchData(1);
        verify(getJobCaller()).processData(1);
        verify(getJobCaller()).processData(2);
        verify(getJobCaller()).processData(3);
        verify(getJobCaller()).processData(4);
        ElasticJobAssert.verifyForIsNotMisfireAndNotStopped(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(2, 2);
    }
}
