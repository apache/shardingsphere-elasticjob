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

package org.apache.shardingsphere.elasticjob.simple.executor;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.simple.job.FooSimpleJob;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimpleJobExecutorTest {
    
    @Mock
    private FooSimpleJob fooSimpleJob;
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private JobRuntimeService jobRuntimeService;
    
    private SimpleJobExecutor jobExecutor;
    
    @BeforeEach
    void setUp() {
        jobExecutor = new SimpleJobExecutor();
    }
    
    @Test
    void assertProcess() {
        jobExecutor.process(fooSimpleJob, jobConfig, jobRuntimeService, any());
        verify(fooSimpleJob, times(1)).execute(any());
    }
    
    @Test
    void assertGetElasticJobClass() {
        assertThat(jobExecutor.getElasticJobClass(), is(SimpleJob.class));
    }
}
