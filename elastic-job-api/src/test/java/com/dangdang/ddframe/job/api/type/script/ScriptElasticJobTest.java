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

package com.dangdang.ddframe.job.api.type.script;

import com.dangdang.ddframe.job.api.internal.executor.JobExceptionHandler;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.exception.JobException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptElasticJobTest {
    
    @Mock
    private JobFacade jobFacade;
    
    @Mock
    private Executor executor;
    
    private ScriptElasticJobExecutor scriptElasticJobExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.getJobName()).thenReturn(ElasticJobAssert.JOB_NAME);
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, ElasticJobAssert.getShardingContext());
        scriptElasticJobExecutor = new ScriptElasticJobExecutor(jobFacade);
        ReflectionUtils.setFieldValue(scriptElasticJobExecutor, "executor", executor);
        scriptElasticJobExecutor.setJobExceptionHandler(new JobExceptionHandler() {
            
            @Override
            public void handleException(final Throwable cause) {
                throw new JobException(cause);
            }
        });
    }
    
    @Test(expected = JobException.class)
    public void assertExecuteWhenScriptCommandLineIsEmpty() throws IOException {
        when(jobFacade.getScriptCommandLine()).thenReturn("");
        scriptElasticJobExecutor.execute();
        verify(executor, times(0)).execute(Matchers.<CommandLine>any());
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = JobException.class)
    public void assertExecuteWhenExecuteFailure() throws IOException {
        when(jobFacade.getScriptCommandLine()).thenReturn("not_exists_file");
        when(executor.execute(Matchers.<CommandLine>any())).thenThrow(IOException.class);
        try {
            scriptElasticJobExecutor.execute();
        } finally {
            verify(executor).execute(Matchers.<CommandLine>any());
        }
    }
    
    @Test
    public void assertExecuteWhenFileExists() throws IOException {
        when(jobFacade.getScriptCommandLine()).thenReturn("exists_file param0 param1");
        scriptElasticJobExecutor.execute();
        verify(jobFacade).getScriptCommandLine();
        verify(executor).execute(Matchers.<CommandLine>any());
    }
}
