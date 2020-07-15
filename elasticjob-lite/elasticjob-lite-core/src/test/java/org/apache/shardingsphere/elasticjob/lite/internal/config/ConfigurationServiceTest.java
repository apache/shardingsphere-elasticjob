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

package org.apache.shardingsphere.elasticjob.lite.internal.config;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.lite.internal.config.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService = new ConfigurationService(null, "test_job");
    
    @Before
    public void setUp() {
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertLoadDirectly() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(false);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCache() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCacheButNull() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(null);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertSetUpJobConfigurationJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobRootNodeExisted()).thenReturn(true);
        when(jobNodeStorage.getJobRootNodeData()).thenReturn("org.apache.shardingsphere.elasticjob.lite.api.script.api.ScriptJob");
        try {
            configService.setUpJobConfiguration(null, JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
        } finally {
            verify(jobNodeStorage).isJobRootNodeExisted();
            verify(jobNodeStorage).getJobRootNodeData();
        }
    }
    
    @Test
    public void assertSetUpJobConfigurationNewJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    public void assertSetUpJobConfigurationExistedJobConfigurationAndOverwrite() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(true).build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    public void assertSetUpJobConfigurationExistedJobConfigurationAndNotOverwrite() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build())));
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(false).build();
        JobConfiguration actual = configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig);
        assertThat(actual, not(jobConfig));
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml(-1));
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            configService.checkMaxTimeDiffSecondsTolerable();
        } finally {
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
}
