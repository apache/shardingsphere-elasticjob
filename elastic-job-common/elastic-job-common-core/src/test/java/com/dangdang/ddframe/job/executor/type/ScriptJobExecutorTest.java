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

package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.exception.JobSystemException;
import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.fixture.ShardingContextsBuilder;
import com.dangdang.ddframe.job.fixture.config.TestScriptJobConfiguration;
import com.dangdang.ddframe.job.fixture.handler.IgnoreJobExceptionHandler;
import com.dangdang.ddframe.job.fixture.handler.ThrowJobExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ScriptJobExecutorTest {
    
    @Mock
    private JobFacade jobFacade;
    
    private ScriptJobExecutor scriptJobExecutor;
    
    @Test
    public void assertExecuteWhenCommandLineIsEmpty() throws IOException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, ShardingContextsBuilder.getMultipleShardingContexts());
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("", IgnoreJobExceptionHandler.class));
        scriptJobExecutor = new ScriptJobExecutor(jobFacade);
        scriptJobExecutor.execute();
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenExecuteFailureForSingleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteWhenExecuteFailure(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    @Test
    public void assertExecuteWhenExecuteFailureForMultipleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteWhenExecuteFailure(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    @SuppressWarnings("unchecked")
    private void assertExecuteWhenExecuteFailure(final ShardingContexts shardingContexts) throws IOException, NoSuchFieldException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("not_exists_file", ThrowJobExceptionHandler.class));
        scriptJobExecutor = new ScriptJobExecutor(jobFacade);
        scriptJobExecutor.execute();
    }
    
    @Test
    public void assertExecuteSuccessForMultipleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteSuccess(ShardingContextsBuilder.getMultipleShardingContexts());
    }
    
    @Test
    public void assertExecuteSuccessForSingleShardingItems() throws IOException, NoSuchFieldException {
        assertExecuteSuccess(ShardingContextsBuilder.getSingleShardingContexts());
    }
    
    private void assertExecuteSuccess(final ShardingContexts shardingContexts) throws IOException, NoSuchFieldException {
        ElasticJobVerify.prepareForIsNotMisfire(jobFacade, shardingContexts);
        when(jobFacade.loadJobRootConfiguration(true)).thenReturn(new TestScriptJobConfiguration("exists_file param0 param1", IgnoreJobExceptionHandler.class));
        scriptJobExecutor = new ScriptJobExecutor(jobFacade);
        scriptJobExecutor.execute();
        verify(jobFacade).loadJobRootConfiguration(true);
    }
}
