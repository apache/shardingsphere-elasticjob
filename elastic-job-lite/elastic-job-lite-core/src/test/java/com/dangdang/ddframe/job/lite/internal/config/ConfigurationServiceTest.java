/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.lite.internal.config;

import com.dangdang.ddframe.job.api.exception.JobConfigurationException;
import com.dangdang.ddframe.job.api.exception.JobExecutionEnvironmentException;
import com.dangdang.ddframe.job.lite.api.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.lite.util.JobConfigurationUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ConfigurationServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private final ConfigurationService configService = new ConfigurationService(null, "test_job");
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertLoadDirectly() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        LiteJobConfiguration actual = configService.load(false);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCache() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        LiteJobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertLoadFromCacheButNull() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(null);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        LiteJobConfiguration actual = configService.load(true);
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertPersistJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn("{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\","
                + "\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        try {
            configService.persist(JobConfigurationUtil.createSimpleLiteJobConfiguration());
        } finally {
            verify(jobNodeStorage).isJobNodeExisted(ConfigurationNode.ROOT);
            verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.ROOT);
        }
    }
    
    @Test
    public void assertPersistNewJobConfiguration() {
        LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration();
        configService.persist(liteJobConfig);
        verify(jobNodeStorage).replaceJobNode("config", LiteJobConfigurationGsonFactory.toJson(liteJobConfig));
    }
    
    @Test
    public void assertPersistExistedJobConfiguration() throws NoSuchFieldException {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn("{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\","
                + "\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":4}");
        LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration(true);
        configService.persist(liteJobConfig);
        verify(jobNodeStorage).replaceJobNode("config", LiteJobConfigurationGsonFactory.toJson(liteJobConfig));
    }
    
    @Test
    public void assertGetShardingItemParametersWhenIsEmpty() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"scriptCommandLine\":\"test.sh\"}");
        assertThat(configService.getShardingItemParameters(), is(Collections.EMPTY_MAP));
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetShardingItemParametersWhenPairFormatInvalid() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"xxx-xxx\",\"scriptCommandLine\":\"test.sh\"}");
        configService.getShardingItemParameters();
    }
    
    @Test(expected = JobConfigurationException.class)
    public void assertGetShardingItemParametersWhenItemIsNotNumber() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"xxx=xxx\",\"scriptCommandLine\":\"test.sh\"}");
        configService.getShardingItemParameters();
    }
    
    @Test
    public void assertGetShardingItemParameters() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"0=A,1=B,2=C\",\"scriptCommandLine\":\"test.sh\"}");
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(configService.getShardingItemParameters(), is(expected));
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"-1\",\"scriptCommandLine\":\"test.sh\"}");
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"60\",\"scriptCommandLine\":\"test.sh\"}");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test(expected = JobExecutionEnvironmentException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() throws JobExecutionEnvironmentException {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"60\",\"scriptCommandLine\":\"test.sh\"}");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            configService.checkMaxTimeDiffSecondsTolerable();
        } finally {
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
}
