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

package com.dangdang.ddframe.job.api.type.integrated;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobFacade;
import com.dangdang.ddframe.job.api.type.ElasticJobAssert;
import com.dangdang.ddframe.job.api.type.util.ScriptElasticJobUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptElasticJobTest {
    
    @Mock
    private JobFacade jobFacade;
    
    private ScriptElasticJob scriptElasticJob;
    
    private String scriptCommandLine;
    
    @Before
    public void setUp() throws NoSuchFieldException, IOException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.getJobName()).thenReturn(ElasticJobAssert.JOB_NAME);
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        scriptElasticJob = new ScriptElasticJob();
        scriptElasticJob.setJobFacade(jobFacade);
        scriptCommandLine = ScriptElasticJobUtil.buildScriptCommandLine();
    }
    
    @Test
    public void assertExecuteWhenFileNotExists() {
        when(jobFacade.getScriptCommandLine()).thenReturn("wrong name");
        scriptElasticJob.execute();
    }
    
    @Test
    public void assertExecuteWhenFileExists() {
        when(jobFacade.getScriptCommandLine()).thenReturn(scriptCommandLine);
        scriptElasticJob.execute();
        verify(jobFacade).getScriptCommandLine();
    }
    
    @Test
    public void assertExecuteWhenFileExistsWithArguments() {
        when(jobFacade.getScriptCommandLine()).thenReturn(scriptCommandLine + " foo bar");
        scriptElasticJob.execute();
        verify(jobFacade).getScriptCommandLine();
    }
}
