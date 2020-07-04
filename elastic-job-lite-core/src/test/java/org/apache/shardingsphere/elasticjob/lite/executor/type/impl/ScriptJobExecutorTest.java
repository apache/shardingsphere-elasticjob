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

package org.apache.shardingsphere.elasticjob.lite.executor.type.impl;

import org.apache.shardingsphere.elasticjob.lite.api.job.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.lite.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.ShardingContexts;
import org.apache.shardingsphere.elasticjob.lite.fixture.ShardingContextsBuilder;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public final class ScriptJobExecutorTest {
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Mock
    private JobFacade jobFacade;
    
    private ElasticJobExecutor elasticJobExecutor;
    
    @Test
    public void assertExecuteWhenCommandLineIsEmpty() {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, ShardingContextsBuilder.getMultipleShardingContexts());
        elasticJobExecutor = new ElasticJobExecutor(regCenter, "SCRIPT", createJobConfiguration("", "IGNORE"), Collections.emptyList(), null);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobFacade", jobFacade);
        elasticJobExecutor.execute();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenExecuteFailureForSingleShardingItems() {
        assertExecuteWhenExecuteFailure(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteWhenExecuteFailureForMultipleShardingItems() {
        assertExecuteWhenExecuteFailure(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    private void assertExecuteWhenExecuteFailure(final ShardingContexts shardingContexts) {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor = new ElasticJobExecutor(regCenter, "SCRIPT", createJobConfiguration("not_exists_file", "THROW"), Collections.emptyList(), null);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobFacade", jobFacade);
        elasticJobExecutor.execute();
    }
    
    @Test
    public void assertExecuteSuccessForMultipleShardingItems() {
        assertExecuteSuccess(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    @Test
    public void assertExecuteSuccessForSingleShardingItems() {
        assertExecuteSuccess(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    private void assertExecuteSuccess(final ShardingContexts shardingContexts) {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        elasticJobExecutor = new ElasticJobExecutor(regCenter, "SCRIPT", createJobConfiguration("exists_file param0 param1", "IGNORE"), Collections.emptyList(), null);
        ReflectionUtils.setFieldValue(elasticJobExecutor, "jobFacade", jobFacade);
        elasticJobExecutor.execute();
    }
    
    private JobConfiguration createJobConfiguration(final String scriptCommandLine, final String jobErrorHandlerType) {
        return JobConfiguration.newBuilder(ShardingContextsBuilder.JOB_NAME, 3)
                .cron("0/1 * * * * ?").jobErrorHandlerType(jobErrorHandlerType).setProperty(ScriptJobExecutor.SCRIPT_KEY, scriptCommandLine).build();
    }
}
