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

package com.dangdang.ddframe.job.api.type.dataflow;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJobExecutor;
import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.dangdang.ddframe.job.api.dataflow.ProcessCountStatistics;
import com.dangdang.ddframe.job.api.internal.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.fixture.JobCaller;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDataflowElasticJobExecutorTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private ShardingContext shardingContext;
    
    private DataflowElasticJobExecutor dataflowElasticJobExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.getJobName()).thenReturn(ElasticJobAssert.JOB_NAME);
        when(jobFacade.getDataflowType()).thenReturn(getDataflowType());
        when(jobFacade.isStreamingProcess()).thenReturn(isStreamingProcess());
        dataflowElasticJobExecutor = new DataflowElasticJobExecutor(createDataflowElasticJob(jobCaller), jobFacade);
        shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
    }
    
    protected abstract DataflowType getDataflowType();
    
    protected abstract boolean isStreamingProcess();
    
    @After
    public void tearDown() throws NoSuchFieldException {
        ProcessCountStatistics.reset(ElasticJobAssert.JOB_NAME);
    }
    
    protected abstract DataflowElasticJob createDataflowElasticJob(final JobCaller jobCaller);
}
