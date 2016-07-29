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

package com.dangdang.ddframe.job.api.type.dataflow.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.fixture.job.JobCaller;
import com.dangdang.ddframe.job.api.fixture.job.TestDataflowJob;
import com.dangdang.ddframe.job.api.fixture.config.TestDataflowJobConfiguration;
import com.dangdang.ddframe.job.api.internal.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractDataflowJobExecutorTest {
    
    private final DataflowJobConfiguration.DataflowType dataflowType;
    
    private final boolean streamingProcess;
    
    private final int concurrentDataProcessThreadCount;
    
    @Mock
    private JobCaller jobCaller;
    
    @Mock
    private JobFacade jobFacade;
    
    private ShardingContext shardingContext;
    
    private DataflowJobExecutor dataflowJobExecutor;
    
    private ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.loadJobConfiguration(true)).thenReturn(new TestDataflowJobConfiguration(dataflowType, streamingProcess, concurrentDataProcessThreadCount));
        shardingContext = ElasticJobAssert.getShardingContext();
        when(jobFacade.getShardingContext()).thenReturn(shardingContext);
        dataflowJobExecutor = new DataflowJobExecutor(new TestDataflowJob(jobCaller), jobFacade);
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
    }
    
    @After
    public void tearDown() throws NoSuchFieldException {
        assertNotNull(ReflectionUtils.getFieldValue(dataflowJobExecutor, AbstractElasticJobExecutor.class.getDeclaredField("executorService")));
        verify(jobFacade).loadJobConfiguration(true);
        ElasticJobAssert.verifyForIsNotMisfire(jobFacade, shardingContext);
    }
    
    
    @Test
    public final void assertExecuteWhenFetchDataIsNullAndEmpty() {
        when(getJobCaller().fetchData(0)).thenReturn(null);
        when(getJobCaller().fetchData(1)).thenReturn(Collections.emptyList());
        getDataflowJobExecutor().execute();
        verify(getJobCaller()).fetchData(0);
        verify(getJobCaller()).fetchData(1);
        verify(getJobCaller(), times(0)).processData(any());
    }
}
