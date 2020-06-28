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

import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.lite.fixture.LiteJsonConstants;
import org.apache.shardingsphere.elasticjob.lite.fixture.TestSimpleJob;
import org.apache.shardingsphere.elasticjob.lite.fixture.util.JobConfigurationUtil;
import org.apache.shardingsphere.elasticjob.lite.internal.config.yaml.YamlJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.lite.util.ReflectionUtils;
import org.apache.shardingsphere.elasticjob.lite.util.yaml.YamlEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
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
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobYaml());
        JobConfiguration actual = configService.load(false);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCache() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCacheButNull() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(null);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertPersistJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobRootNodeExisted()).thenReturn(true);
        when(jobNodeStorage.getJobRootNodeData()).thenReturn("org.apache.shardingsphere.elasticjob.lite.api.script.api.ScriptJob");
        try {
            configService.persist(null, JobConfigurationUtil.createSimpleJobConfiguration());
        } finally {
            verify(jobNodeStorage).isJobRootNodeExisted();
            verify(jobNodeStorage).getJobRootNodeData();
        }
    }
    
    @Test
    public void assertPersistNewJobConfiguration() {
        JobConfiguration jobConfig = JobConfigurationUtil.createSimpleJobConfiguration();
        configService.persist(TestSimpleJob.class.getName(), jobConfig);
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(YamlJobConfiguration.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    public void assertPersistExistedJobConfiguration() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        JobConfiguration jobConfig = JobConfigurationUtil.createSimpleJobConfiguration(true);
        configService.persist(TestSimpleJob.class.getName(), jobConfig);
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(YamlJobConfiguration.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobJson(-1));
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteJsonConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            configService.checkMaxTimeDiffSecondsTolerable();
        } finally {
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
}
