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
import com.dangdang.ddframe.job.plugin.job.type.fixture.FooStreamingIndividualThroughputDataFlowElasticJob;
import com.dangdang.ddframe.job.plugin.job.type.fixture.JobCaller;

public final class StreamingIndividualThroughputDataFlowElasticJobTest {
    
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
    
    private JobExecutionMultipleShardingContext shardingContext;
    
    private FooStreamingIndividualThroughputDataFlowElasticJob streamingIndividualThroughputDataFlowElasticJob;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(configService.getJobName()).thenReturn(ElasticJobAssert.JOB_NAME);
        streamingIndividualThroughputDataFlowElasticJob = new FooStreamingIndividualThroughputDataFlowElasticJob(jobCaller);
        streamingIndividualThroughputDataFlowElasticJob.setConfigService(configService);
        streamingIndividualThroughputDataFlowElasticJob.setShardingService(shardingService);
        streamingIndividualThroughputDataFlowElasticJob.setExecutionContextService(executionContextService);
        streamingIndividualThroughputDataFlowElasticJob.setExecutionService(executionService);
        streamingIndividualThroughputDataFlowElasticJob.setFailoverService(failoverService);
        streamingIndividualThroughputDataFlowElasticJob.setOffsetService(offsetService);
        shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfireAndIsNotFailover(configService, executionContextService, executionService, shardingContext);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        ProcessCountStatistics.reset(ElasticJobAssert.JOB_NAME);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNull() throws JobExecutionException {
        when(jobCaller.fetchData()).thenReturn(null);
        streamingIndividualThroughputDataFlowElasticJob.execute(null);
        verify(jobCaller).fetchData();
        verify(jobCaller, times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsEmpty() throws JobExecutionException {
        when(jobCaller.fetchData()).thenReturn(Collections.emptyList());
        streamingIndividualThroughputDataFlowElasticJob.execute(null);
        verify(jobCaller).fetchData();
        verify(jobCaller, times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsStoped() throws JobExecutionException {
        when(jobCaller.fetchData()).thenReturn(Arrays.<Object>asList(1));
        streamingIndividualThroughputDataFlowElasticJob.stop();
        streamingIndividualThroughputDataFlowElasticJob.execute(null);
        verify(jobCaller).fetchData();
        verify(jobCaller, times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
        streamingIndividualThroughputDataFlowElasticJob.resume();
    }
    
    @Test
    public void assertExecuteWhenFetchDataIsNotEmptyAndIsNeedSharding() throws JobExecutionException {
        when(jobCaller.fetchData()).thenReturn(Arrays.<Object>asList(1));
        when(shardingService.isNeedSharding()).thenReturn(true);
        streamingIndividualThroughputDataFlowElasticJob.execute(null);
        verify(shardingService).isNeedSharding();
        verify(jobCaller, times(0)).processData(any());
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(0, 0);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertExecuteWhenFetchDataIsNotEmpty() throws JobExecutionException {
        when(jobCaller.fetchData()).thenReturn(Arrays.<Object>asList(1, 2), Collections.<Object>emptyList());
        when(shardingService.isNeedSharding()).thenReturn(false);
        when(jobCaller.processData(1)).thenReturn(false);
        when(jobCaller.processData(2)).thenReturn(true);
        streamingIndividualThroughputDataFlowElasticJob.execute(null);
        verify(shardingService).isNeedSharding();
        verify(jobCaller, times(2)).fetchData();
        verify(jobCaller).processData(1);
        verify(jobCaller).processData(2);
        ElasticJobAssert.verifyForIsNotMisfireAndIsNotFailover(configService, shardingService, executionContextService, executionService, failoverService, shardingContext);
        ElasticJobAssert.assertProcessCountStatistics(1, 1);
    }
}
