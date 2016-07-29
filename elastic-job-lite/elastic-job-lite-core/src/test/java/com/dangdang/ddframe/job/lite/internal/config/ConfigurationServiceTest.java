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

import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ConfigurationServiceTest {
    
    @Mock
    private JobNodeStorage jobNodeStorage;
    
    private final LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration();
    
    private final ConfigurationService configService = new ConfigurationService(null, liteJobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test
    public void assertLoad() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        LiteJobConfiguration actual = configService.load();
        assertThat(actual.getJobName(), is("test_job"));
        assertThat(actual.getTypeConfig().getCoreConfig().getCron(), is("0/1 * * * * ?"));
        assertThat(actual.getTypeConfig().getCoreConfig().getShardingTotalCount(), is(3));
    }
    
    @Test(expected = JobConflictException.class)
    public void assertPersistJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn("{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\","
                + "\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":3,\"scriptCommandLine\":\"test.sh\"}");
        when(jobNodeStorage.getLiteJobConfig()).thenReturn(liteJobConfig);
        try {
            configService.persist();
        } finally {
            verify(jobNodeStorage).isJobNodeExisted(ConfigurationNode.ROOT);
            verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.ROOT);
            verify(jobNodeStorage, times(3)).getLiteJobConfig();
        }
    }
    
    @Test
    public void assertPersistNewJobConfiguration() {
        when(jobNodeStorage.getLiteJobConfig()).thenReturn(liteJobConfig);
        configService.persist();
        verify(jobNodeStorage).replaceJobNode("config", LiteJobConfigurationGsonFactory.getGson().toJson(liteJobConfig));
    }
    
    @Test
    public void assertPersistExistedJobConfiguration() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.ROOT)).thenReturn(true);
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn("{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.lite.fixture.TestSimpleJob\","
                + "\"jobType\":\"SIMPLE\",\"cron\":\"0/1 * * * * ?\",\"shardingTotalCount\":4}");
        LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createSimpleLiteJobConfiguration(true);
        when(jobNodeStorage.getLiteJobConfig()).thenReturn(liteJobConfig);
        configService.persist();
        verify(jobNodeStorage).replaceJobNode("config", LiteJobConfigurationGsonFactory.getGson().toJson(liteJobConfig));
    }
    
    @Test
    public void assertGetShardingItemParametersWhenIsEmpty() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"\",\"scriptCommandLine\":\"test.sh\"}");
        assertThat(configService.getShardingItemParameters(), is(Collections.EMPTY_MAP));
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenPairFormatInvalid() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"xxx-xxx\",\"scriptCommandLine\":\"test.sh\"}");
        configService.getShardingItemParameters();
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenItemIsNotNumber() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"xxx=xxx\",\"scriptCommandLine\":\"test.sh\"}");
        configService.getShardingItemParameters();
    }
    
    @Test
    public void assertGetShardingItemParameters() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"shardingItemParameters\":\"0=A,1=B,2=C\",\"scriptCommandLine\":\"test.sh\"}");
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(configService.getShardingItemParameters(), is(expected));
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"-1\",\"scriptCommandLine\":\"test.sh\"}");
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"60\",\"scriptCommandLine\":\"test.sh\"}");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"maxTimeDiffSeconds\":\"60\",\"scriptCommandLine\":\"test.sh\"}");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            configService.checkMaxTimeDiffSecondsTolerable();
        } finally {
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
    
    @Test
    public void assertIsNotFailoverWhenNotMonitorExecution() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"failover\":false,\"monitorExecution\":false,\"scriptCommandLine\":\"test.sh\"}");
        assertFalse(configService.isFailover());
    }
    
    @Test
    public void assertIsNotFailoverWhenMonitorExecution() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"failover\":false,\"monitorExecution\":true,\"scriptCommandLine\":\"test.sh\"}");
        assertFalse(configService.isFailover());
    }
    
    @Test
    public void assertIsFailover() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.ROOT)).thenReturn(
                "{\"jobName\":\"test_job\",\"jobClass\":\"com.dangdang.ddframe.job.api.type.script.api.ScriptJob\",\"jobType\":\"SCRIPT\",\"cron\":\"0/1 * * * * ?\","
                        + "\"shardingTotalCount\":3,\"failover\":true,\"monitorExecution\":true,\"scriptCommandLine\":\"test.sh\"}");
        assertTrue(configService.isFailover());
    }
}
