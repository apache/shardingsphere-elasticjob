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

import org.apache.shardingsphere.elasticjob.lite.api.type.JobType;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobSettingsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobSettings;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.fixture.LifecycleYamlConstants;
import org.apache.shardingsphere.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
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
public final class JobSettingsAPIImplTest {
    
    private JobSettingsAPI jobSettingsAPI;
    
    @Mock
    private CoordinatorRegistryCenter regCenter;
    
    @Before
    public void setUp() {
        jobSettingsAPI = new JobSettingsAPIImpl(regCenter);
    }
    
    @Test
    public void assertGetDataflowJobSettings() {
        when(regCenter.get("/test_job/config")).thenReturn(LifecycleYamlConstants.getDataflowJobYaml());
        JobSettings actual = jobSettingsAPI.getJobSettings("test_job");
        assertJobSettings(actual);
        assertTrue(actual.isStreamingProcess());
        verify(regCenter).get("/test_job/config");
    }
    
    @Test
    public void assertGetScriptJobSettings() {
        when(regCenter.get("/test_job/config")).thenReturn(LifecycleYamlConstants.getScriptJobYaml());
        JobSettings actual = jobSettingsAPI.getJobSettings("test_job");
        assertJobSettings(actual);
        assertThat(actual.getScriptCommandLine(), is("echo"));
        verify(regCenter).get("/test_job/config");
    }
    
    private void assertJobSettings(final JobSettings jobSettings) {
        assertThat(jobSettings.getJobName(), is("test_job"));
        assertThat(jobSettings.getShardingTotalCount(), is(3));
        assertThat(jobSettings.getCron(), is("0/1 * * * * ?"));
        assertThat(jobSettings.getShardingItemParameters(), is(""));
        assertThat(jobSettings.getJobParameter(), is("param"));
        assertThat(jobSettings.isMonitorExecution(), is(true));
        assertThat(jobSettings.getMaxTimeDiffSeconds(), is(-1));
        assertFalse(jobSettings.isFailover());
        assertTrue(jobSettings.isMisfire());
        assertNull(jobSettings.getJobShardingStrategyType());
        assertThat(jobSettings.getReconcileIntervalMinutes(), is(10));
        assertThat(jobSettings.getDescription(), is(""));
    }
    
    @Test
    public void assertUpdateJobSettings() {
        JobSettings jobSettings = new JobSettings();
        jobSettings.setJobName("test_job");
        jobSettings.setJobType(JobType.DATAFLOW.toString());
        jobSettings.setCron("0/1 * * * * ?");
        jobSettings.setShardingTotalCount(3);
        jobSettings.setJobParameter("param");
        jobSettings.setMonitorExecution(true);
        jobSettings.setFailover(false);
        jobSettings.setMisfire(true);
        jobSettings.setMaxTimeDiffSeconds(-1);
        jobSettings.setReconcileIntervalMinutes(10);
        jobSettings.setDescription("");
        jobSettings.setStreamingProcess(true);
        jobSettingsAPI.updateJobSettings(jobSettings);
        verify(regCenter).update("/test_job/config", LifecycleYamlConstants.getDataflowJobYaml());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobSettingsIfJobNameIsEmpty() {
        JobSettings jobSettings = new JobSettings();
        jobSettings.setJobName("");
        jobSettingsAPI.updateJobSettings(jobSettings);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobSettingsIfCronIsEmpty() {
        JobSettings jobSettings = new JobSettings();
        jobSettings.setJobName("test_job");
        jobSettings.setCron("");
        jobSettingsAPI.updateJobSettings(jobSettings);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertUpdateJobSettingsIfShardingTotalCountLessThanOne() {
        JobSettings jobSettings = new JobSettings();
        jobSettings.setJobName("test_job");
        jobSettings.setCron("0/1 * * * * ?");
        jobSettings.setShardingTotalCount(0);
        jobSettingsAPI.updateJobSettings(jobSettings);
    }
    
    @Test
    public void assertRemoveJobSettings() {
        jobSettingsAPI.removeJobSettings("test_job");
        verify(regCenter).remove("/test_job");
    }
}
