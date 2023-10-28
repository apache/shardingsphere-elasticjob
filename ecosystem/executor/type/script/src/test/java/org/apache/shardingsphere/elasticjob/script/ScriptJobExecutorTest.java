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

package org.apache.shardingsphere.elasticjob.script;

import org.apache.commons.exec.OS;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.executor.item.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.script.executor.ScriptJobExecutor;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptJobExecutorTest {
    
    @Mock
    private ElasticJob elasticJob;
    
    @Mock
    private JobConfiguration jobConfig;
    
    @Mock
    private JobRuntimeService jobRuntimeService;
    
    @Mock
    private Properties properties;
    
    @Mock
    private ShardingContext shardingContext;
    
    private ScriptJobExecutor jobExecutor;
    
    @BeforeEach
    void setUp() {
        jobExecutor = new ScriptJobExecutor();
    }
    
    @Test
    void assertProcessWithJobConfigurationException() {
        assertThrows(JobConfigurationException.class, () -> {
            when(jobConfig.getProps()).thenReturn(properties);
            jobExecutor.process(elasticJob, jobConfig, jobRuntimeService, shardingContext);
        });
    }
    
    @Test
    void assertProcessWithJobSystemException() {
        assertThrows(JobSystemException.class, () -> {
            when(jobConfig.getProps()).thenReturn(properties);
            when(properties.getProperty(ScriptJobProperties.SCRIPT_KEY)).thenReturn("demo.sh");
            jobExecutor.process(elasticJob, jobConfig, jobRuntimeService, shardingContext);
        });
    }
    
    @Test
    void assertProcess() {
        when(jobConfig.getProps()).thenReturn(properties);
        when(properties.getProperty(ScriptJobProperties.SCRIPT_KEY)).thenReturn(determineCommandByPlatform());
        jobExecutor.process(elasticJob, jobConfig, jobRuntimeService, shardingContext);
    }
    
    private String determineCommandByPlatform() {
        return OS.isFamilyWindows() ? getWindowsEcho() : getEcho();
    }
    
    private String getWindowsEcho() {
        return "cmd /c echo script-job";
    }
    
    private String getEcho() {
        return "echo script-job";
    }
    
    @Test
    void assertGetType() {
        assertThat(jobExecutor.getType(), is("SCRIPT"));
    }
}
