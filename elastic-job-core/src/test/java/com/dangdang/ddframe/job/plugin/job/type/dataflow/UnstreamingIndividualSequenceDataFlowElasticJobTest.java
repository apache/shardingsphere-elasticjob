/**
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

package com.dangdang.ddframe.job.plugin.job.type.dataflow;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionException;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import com.dangdang.ddframe.job.plugin.job.type.fixture.FooUnstreamingIndividualSequenceDataFlowElasticJob;
import com.dangdang.ddframe.job.plugin.job.type.fixture.JobCaller;

public final class UnstreamingIndividualSequenceDataFlowElasticJobTest {
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private OffsetService offsetService;
    
    private FooUnstreamingIndividualSequenceDataFlowElasticJob unstreamingIndividualSequenceDataFlowElasticJob;
    
    private JobExecutionMultipleShardingContext shardingContext;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(configService.getJobName()).thenReturn("testJob");
        unstreamingIndividualSequenceDataFlowElasticJob = new FooUnstreamingIndividualSequenceDataFlowElasticJob(jobCaller);
        unstreamingIndividualSequenceDataFlowElasticJob.setConfigService(configService);
        unstreamingIndividualSequenceDataFlowElasticJob.setShardingService(shardingService);
        unstreamingIndividualSequenceDataFlowElasticJob.setExecutionContextService(executionContextService);
        unstreamingIndividualSequenceDataFlowElasticJob.setExecutionService(executionService);
        unstreamingIndividualSequenceDataFlowElasticJob.setFailoverService(failoverService);
        unstreamingIndividualSequenceDataFlowElasticJob.setOffsetService(offsetService);
        shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfireAndIsNotFailover(configService, executionContextService, executionService, shardingContext);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        ProcessCountStatistics.reset(ElasticJobAssert.JOB_NAME);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNullAndEmpty() throws JobExecutionException {
        when(jobCaller.fetchData(0)).thenReturn(null);
        when(jobCaller.fetchData(1)).thenReturn(Collections.emptyList());
        unstreamingIndividualSequenceDataFlowElasticJob.execute(null);
        verify(jobCaller).fetchData(0);
        verify(jobCaller).fetchData(1);
        verify(jobCaller, times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() throws JobExecutionException {
        when(jobCaller.fetchData(0)).thenReturn(Arrays.<Object>asList(1, 2));
        when(jobCaller.fetchData(1)).thenReturn(Arrays.<Object>asList(3, 4));
        when(jobCaller.processData(1)).thenReturn(true);
        when(jobCaller.processData(2)).thenReturn(true);
        when(jobCaller.processData(4)).thenThrow(new RuntimeException());
        unstreamingIndividualSequenceDataFlowElasticJob.execute(null);
        verify(jobCaller).fetchData(0);
        verify(jobCaller).fetchData(1);
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        verify(jobCaller).processData(3);
        verify(jobCaller).processData(4);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(2, 2);
    }
}
