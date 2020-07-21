/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.dataflow.executor;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataflowJobExecutorTest {
    
    private DataflowJobExecutor jobExecutor;
    
    @Mock
    private DataflowJob elasticJob;
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private ShardingContext shardingContext;
    
    @Mock
    private Properties properties;
    
    @Before
    public void createJobExecutor() {
        jobExecutor = new DataflowJobExecutor();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertProcessWithStreamingExecute() {
        List<String> data = Arrays.asList("DataflowJob1", "DataflowJob2");
        when(jobConfig.getProps()).thenReturn(properties);
        when(properties.getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false)).thenReturn("true");
        when(elasticJob.fetchData(shardingContext)).thenReturn(data);
        when(jobFacade.isNeedSharding()).thenReturn(true);
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
        verify(elasticJob, times(1)).processData(shardingContext, data);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertProcessWithOneOffExecute() {
        List<String> data = Arrays.asList("DataflowJob1", "DataflowJob2");
        when(jobConfig.getProps()).thenReturn(properties);
        when(properties.getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false)).thenReturn("false");
        when(elasticJob.fetchData(shardingContext)).thenReturn(data);
        jobExecutor.process(elasticJob, jobConfig, jobFacade, shardingContext);
        verify(elasticJob, times(1)).processData(shardingContext, data);
    }
    
    @Test
    public void assertGetElasticJobClass() {
        assertThat(jobExecutor.getElasticJobClass(), is(DataflowJob.class));
    }
}
