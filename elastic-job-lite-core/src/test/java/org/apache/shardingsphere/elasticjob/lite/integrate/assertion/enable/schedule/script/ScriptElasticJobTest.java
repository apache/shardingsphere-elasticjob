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

package org.apache.shardingsphere.elasticjob.lite.integrate.assertion.enable.schedule.script;

import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.integrate.EnabledJobIntegrateTest;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.job.impl.ScriptJob;
import org.apache.shardingsphere.elasticjob.lite.util.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.lite.util.yaml.YamlEngine;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ScriptElasticJobTest extends EnabledJobIntegrateTest {
    
    public ScriptElasticJobTest() {
        super(TestType.SCHEDULE, new ScriptJob());
    }
    
    @Override
    protected JobConfiguration getJobConfiguration(final String jobName) {
        return JobConfiguration.newBuilder(jobName, 3).cron("0/1 * * * * ?").shardingItemParameters("0=A,1=B,2=C").overwrite(true).setProperty(ScriptJob.SCRIPT_KEY, "echo").build();
    }
    
    @Test
    public void assertJobInit() {
        BlockUtils.waitingShortTime();
        String scriptCommandLine = getJobConfiguration().getProps().getProperty(ScriptJob.SCRIPT_KEY);
        JobConfiguration jobConfig = YamlEngine.unmarshal(getRegCenter().get("/" + getJobName() + "/config"), YamlJobConfiguration.class).toJobConfiguration();
        assertThat(jobConfig.getProps().getProperty(ScriptJob.SCRIPT_KEY), is(scriptCommandLine));
    }
}
