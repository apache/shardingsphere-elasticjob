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

package org.apache.shardingsphere.elasticjob.lite.job.impl;

import org.apache.shardingsphere.elasticjob.lite.api.job.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobSystemException;
import org.junit.Test;

import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class ScriptJobTest {
    
    @Test(expected = JobConfigurationException.class)
    public void assertExecuteWhenCommandLineIsEmpty() {
        ScriptJob scriptJob = new ScriptJob();
        scriptJob.init(new Properties());
        scriptJob.execute(mock(ShardingContext.class));
    }
    
    @Test(expected = JobSystemException.class)
    public void assertExecuteWhenExecuteFailure() {
        ScriptJob scriptJob = new ScriptJob();
        Properties props = new Properties();
        props.setProperty(ScriptJob.SCRIPT_KEY, "not_exists_file");
        scriptJob.init(props);
        scriptJob.execute(mock(ShardingContext.class));
    }
    
    @Test
    public void assertExecuteWhenExecuteSuccess() {
        ScriptJob scriptJob = new ScriptJob();
        Properties props = new Properties();
        props.setProperty(ScriptJob.SCRIPT_KEY, "echo");
        scriptJob.init(props);
        scriptJob.execute(mock(ShardingContext.class));
    }
}
