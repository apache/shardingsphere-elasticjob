/*
 *
 *  * Copyright 1999-2015 dangdang.com.
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * </p>
 *  
 */

package com.dangdang.ddframe.job.plugin.job.type.integrated;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.fixture.ScriptElasticJobHelper;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.plugin.job.type.ElasticJobAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptElasticJobTest {
    
    @Mock
    private JobFacade jobFacade;
    
    private ScriptElasticJob scriptElasticJob;
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    private String scriptCommandLine;
    
    @Before
    public void setUp() throws NoSuchFieldException, IOException {
        MockitoAnnotations.initMocks(this);
        when(jobFacade.getJobName()).thenReturn(ElasticJobAssert.JOB_NAME);
        JobExecutionMultipleShardingContext shardingContext = ElasticJobAssert.getShardingContext();
        ElasticJobAssert.prepareForIsNotMisfire(jobFacade, shardingContext);
        scriptElasticJob = new ScriptElasticJob();
        scriptElasticJob.setJobFacade(jobFacade);
        scriptCommandLine = ScriptElasticJobHelper.buildScriptCommandLine();
    }
    
    @Test(expected = JobExecutionException.class)
    public void assertExecuteWhenFileNotExists() throws JobExecutionException {
        when(jobFacade.getScriptCommandLine()).thenReturn("wrong name");
        scriptElasticJob.execute(jobExecutionContext);
    }
    
    @Test
    public void assertExecuteWhenFileExists() throws JobExecutionException {
        when(jobFacade.getScriptCommandLine()).thenReturn(scriptCommandLine);
        scriptElasticJob.execute(jobExecutionContext);
        verify(jobFacade).getScriptCommandLine();
    }
    
    @Test
    public void assertExecuteWhenFileExistsWithArguments() throws JobExecutionException {
        when(jobFacade.getScriptCommandLine()).thenReturn(scriptCommandLine + " foo bar");
        scriptElasticJob.execute(jobExecutionContext);
        verify(jobFacade).getScriptCommandLine();
    }
}
