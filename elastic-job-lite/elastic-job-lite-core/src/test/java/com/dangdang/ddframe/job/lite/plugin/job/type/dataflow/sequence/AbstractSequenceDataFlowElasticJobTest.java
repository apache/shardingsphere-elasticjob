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

package com.dangdang.ddframe.job.lite.plugin.job.type.dataflow.sequence;

import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Test;

import com.dangdang.ddframe.job.lite.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.lite.plugin.job.type.dataflow.AbstractDataFlowElasticJobTest;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSequenceDataFlowElasticJobTest extends AbstractDataFlowElasticJobTest {
    
    @Test
    public void assertExecuteWhenFetchDataIsNullAndEmpty() {
        when(getJobCaller().fetchData(0)).thenReturn(null);
        when(getJobCaller().fetchData(1)).thenReturn(Collections.emptyList());
        getDataFlowElasticJob().execute();
        verify(getJobCaller()).fetchData(0);
        verify(getJobCaller()).fetchData(1);
        verify(getJobCaller(), times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfire(getJobFacade(), getShardingContext());
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
}
