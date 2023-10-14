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

package org.apache.shardingsphere.elasticjob.engine.internal.config;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;
import org.apache.shardingsphere.elasticjob.infra.exception.JobExecutionEnvironmentException;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.yaml.YamlEngine;
import org.apache.shardingsphere.elasticjob.engine.fixture.LiteYamlConstants;
import org.apache.shardingsphere.elasticjob.engine.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.engine.util.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService = new ConfigurationService(null, "test_job");
    
    @BeforeEach
    void setUp() {
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    void assertLoadDirectly() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(false);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    void assertLoadFromCache() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    void assertLoadFromCacheButNull() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(null);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        JobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getShardingTotalCount(), is(3));
    }
    
    @Test
    void assertSetUpJobConfigurationJobConfigurationForJobConflict() {
        assertThrows(JobConfigurationException.class, () -> {
            when(jobNodeStorage.isJobRootNodeExisted()).thenReturn(true);
            when(jobNodeStorage.getJobRootNodeData()).thenReturn("org.apache.shardingsphere.elasticjob.engine.api.script.api.ScriptJob");
            try {
                configService.setUpJobConfiguration(null, JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build());
            } finally {
                verify(jobNodeStorage).isJobRootNodeExisted();
                verify(jobNodeStorage).getJobRootNodeData();
            }
        });
    }
    
    @Test
    void assertSetUpJobConfigurationNewJobConfiguration() {
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    void assertSetUpJobConfigurationExistedJobConfigurationAndOverwrite() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(true).build();
        assertThat(configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig), is(jobConfig));
        verify(jobNodeStorage).replaceJobNode("config", YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(jobConfig)));
    }
    
    @Test
    void assertSetUpJobConfigurationExistedJobConfigurationAndNotOverwrite() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                YamlEngine.marshal(JobConfigurationPOJO.fromJobConfiguration(JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").build())));
        JobConfiguration jobConfig = JobConfiguration.newBuilder("test_job", 3).cron("0/1 * * * * ?").overwrite(false).build();
        JobConfiguration actual = configService.setUpJobConfiguration(ElasticJob.class.getName(), jobConfig);
        assertThat(actual, not(jobConfig));
    }
    
    @Test
    void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml(-1));
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    void assertIsMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test
    void assertIsNotMaxTimeDiffSecondsTolerable() {
        assertThrows(JobExecutionEnvironmentException.class, () -> {
            when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(LiteYamlConstants.getJobYaml());
            when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
            try {
                configService.checkMaxTimeDiffSecondsTolerable();
            } finally {
                verify(jobNodeStorage).getRegistryCenterTime();
            }
        });
    }
}
