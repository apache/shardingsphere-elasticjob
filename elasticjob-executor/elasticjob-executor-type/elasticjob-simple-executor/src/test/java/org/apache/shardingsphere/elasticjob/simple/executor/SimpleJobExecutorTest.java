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
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.simple.job.FooSimpleJob;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SimpleJobExecutorTest {
    
    @Mock
    private FooSimpleJob fooSimpleJob;
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private JobFacade jobFacade;
    
    private SimpleJobExecutor jobExecutor;
    
    @Before
    public void setUp() {
        jobExecutor = new SimpleJobExecutor();
    }
    
    @Test
    public void assertProcess() {
        jobExecutor.process(fooSimpleJob, jobConfig, jobFacade, any());
        verify(fooSimpleJob, times(1)).execute(any());
    }
    
    @Test
    public void assertGetElasticJobClass() {
        assertThat(jobExecutor.getElasticJobClass(), is(SimpleJob.class));
    }
}
