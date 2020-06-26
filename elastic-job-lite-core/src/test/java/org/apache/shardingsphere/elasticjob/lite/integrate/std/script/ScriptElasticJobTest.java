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

package org.apache.shardingsphere.elasticjob.lite.integrate.std.script;

import org.apache.shardingsphere.elasticjob.lite.api.script.ScriptJob;
import org.apache.shardingsphere.elasticjob.lite.config.LiteJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.fixture.util.ScriptElasticJobUtil;
import org.apache.shardingsphere.elasticjob.lite.integrate.AbstractBaseStdJobAutoInitTest;
import org.apache.shardingsphere.elasticjob.lite.integrate.WaitingUtils;
import org.apache.shardingsphere.elasticjob.lite.internal.config.json.LiteJobConfigurationGsonFactory;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ScriptElasticJobTest extends AbstractBaseStdJobAutoInitTest {
    
    public ScriptElasticJobTest() {
        super(ScriptJob.class);
    }
    
    @Test
    public void assertJobInit() throws IOException {
        ScriptElasticJobUtil.buildScriptCommandLine();
        WaitingUtils.waitingShortTime();
        String scriptCommandLine = ((ScriptJobConfiguration) getLiteJobConfig().getTypeConfig()).getScriptCommandLine();
        LiteJobConfiguration liteJobConfig = LiteJobConfigurationGsonFactory.fromJson(getRegCenter().get("/" + getJobName() + "/config"));
        assertThat(((ScriptJobConfiguration) liteJobConfig.getTypeConfig()).getScriptCommandLine(), is(scriptCommandLine));
    }
}
