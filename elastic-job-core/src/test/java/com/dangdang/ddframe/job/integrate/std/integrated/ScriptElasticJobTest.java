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

package com.dangdang.ddframe.job.integrate.std.integrated;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.fixture.ScriptElasticJobHelper;
import com.dangdang.ddframe.job.integrate.AbstractBaseStdJobAutoInitTest;
import com.dangdang.ddframe.job.integrate.WaitingUtils;
import com.dangdang.ddframe.job.plugin.job.type.integrated.ScriptElasticJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ScriptElasticJobTest extends AbstractBaseStdJobAutoInitTest {
    
    private String scriptCommandLine = ScriptElasticJobTest.class.getResource("/script/test.sh").getPath();
    
    public ScriptElasticJobTest() {
        super(ScriptElasticJob.class);
    }
    
    @Override
    protected void setJobConfig(final JobConfiguration jobConfig) {
        jobConfig.setScriptCommandLine(scriptCommandLine);
    }
    
    @Test
    public void assertJobInit() {
        ScriptElasticJobHelper.buildScriptCommandLine();
        WaitingUtils.waitingShortTime();
        assertThat(getRegCenter().get("/" + getJobName() + "/config/scriptCommandLine"), is(scriptCommandLine));
    }
}
