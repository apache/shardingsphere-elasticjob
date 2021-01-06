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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings;

import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.fixture.LifecycleYamlConstants;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.script.props.ScriptJobProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JobConfigurationAPIImplTest {
    
    private JobConfigurationAPI jobConfigAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        jobConfigAPI = new JobConfigurationAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetDataflowJobConfig() {
        when(regCenter.get("/test_job/config")).thenReturn(LifecycleYamlConstants.getDataflowJobYaml());
        JobConfigurationPOJO actual = jobConfigAPI.getJobConfiguration("test_job");
        assertJobConfig(actual);
        assertThat(actual.getProps().getProperty(DataflowJobProperties.STREAM_PROCESS_KEY), is("true"));
        verify(regCenter).get("/test_job/config");
    }
    
    @Test
    public void assertGetScriptJobConfig() {
        when(regCenter.get("/test_job/config")).thenReturn(LifecycleYamlConstants.getScriptJobYaml());
        JobConfigurationPOJO actual = jobConfigAPI.getJobConfiguration("test_job");
        assertJobConfig(actual);
        assertThat(actual.getProps().getProperty(ScriptJobProperties.SCRIPT_KEY), is("echo"));
        verify(regCenter).get("/test_job/config");
    }
    
    private void assertJobConfig(final JobConfigurationPOJO pojo) {
        assertThat(pojo.getJobName(), is("test_job"));
        assertThat(pojo.getShardingTotalCount(), is(3));
        assertThat(pojo.getCron(), is("0/1 * * * * ?"));
        assertNull(pojo.getShardingItemParameters());
        assertThat(pojo.getJobParameter(), is("param"));
        assertThat(pojo.isMonitorExecution(), is(true));
        assertThat(pojo.getMaxTimeDiffSeconds(), is(-1));
        assertFalse(pojo.isFailover());
        assertTrue(pojo.isMisfire());
        assertNull(pojo.getJobShardingStrategyType());
        assertThat(pojo.getReconcileIntervalMinutes(), is(10));
        assertThat(pojo.getDescription(), is(""));
    }
    
    @Test
    public void assertUpdateJobConfig() {
        JobConfigurationPOJO jobConfiguration = new JobConfigurationPOJO();
        jobConfiguration.setJobName("test_job");
        jobConfiguration.setCron("0/1 * * * * ?");
        jobConfiguration.setShardingTotalCount(3);
        jobConfiguration.setJobParameter("param");
        jobConfiguration.setMonitorExecution(true);
        jobConfiguration.setFailover(false);
        jobConfiguration.setMisfire(true);
        jobConfiguration.setMaxTimeDiffSeconds(-1);
        jobConfiguration.setReconcileIntervalMinutes(10);
        jobConfiguration.setDescription("");
        jobConfiguration.getProps().setProperty(DataflowJobProperties.STREAM_PROCESS_KEY, "true");
        jobConfigAPI.updateJobConfiguration(jobConfiguration);
        verify(regCenter).update("/test_job/config", LifecycleYamlConstants.getDataflowJobYaml());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobConfigIfJobNameIsEmpty() {
        JobConfigurationPOJO jobConfiguration = new JobConfigurationPOJO();
        jobConfiguration.setJobName("");
        jobConfigAPI.updateJobConfiguration(jobConfiguration);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobConfigIfCronIsEmpty() {
        JobConfigurationPOJO jobConfiguration = new JobConfigurationPOJO();
        jobConfiguration.setJobName("test_job");
        jobConfiguration.setCron("");
        jobConfigAPI.updateJobConfiguration(jobConfiguration);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobConfigIfShardingTotalCountLessThanOne() {
        JobConfigurationPOJO jobConfiguration = new JobConfigurationPOJO();
        jobConfiguration.setJobName("test_job");
        jobConfiguration.setCron("0/1 * * * * ?");
        jobConfiguration.setShardingTotalCount(0);
        jobConfigAPI.updateJobConfiguration(jobConfiguration);
    }
    
    @Test
    public void assertRemoveJobConfiguration() {
        jobConfigAPI.removeJobConfiguration("test_job");
        verify(regCenter).remove("/test_job");
    }
}
