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

import com.dangdang.ddframe.job.api.job.dataflow.DataFlowElasticJob;
import com.dangdang.ddframe.job.api.type.integrated.ScriptElasticJob;
import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.JobConfigurationFactory;
import com.dangdang.ddframe.job.lite.api.config.impl.DataFlowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.config.impl.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestJob;
import com.dangdang.ddframe.job.lite.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
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
    
    private final JobConfiguration jobConfig = JobConfigurationFactory.createSimpleJobConfigurationBuilder("testJob", TestJob.class, 3, "0/1 * * * * ?").build();
    
    private final ConfigurationService configService = new ConfigurationService(null, jobConfig);
    
    @Before
    public void initMocks() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(configService, "jobNodeStorage", jobNodeStorage);
    }
    
    @Test(expected = JobConflictException.class)
    public void assertPersistJobConfigurationForJobConflict() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.JOB_CLASS)).thenReturn(true);
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.JOB_CLASS)).thenReturn("ConflictJob");
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        try {
            configService.persistJobConfiguration();
        } finally {
            verify(jobNodeStorage).isJobNodeExisted(ConfigurationNode.JOB_CLASS);
            verify(jobNodeStorage).getJobNodeData(ConfigurationNode.JOB_CLASS);
            verify(jobNodeStorage, times(2)).getJobConfiguration();
        }
    }
    
    @Test
    public void assertPersistNewJobConfiguration() {
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        configService.persistJobConfiguration();
        verifyPersistJobConfiguration();
    }
    
    @Test
    public void assertPersistExistedJobConfiguration() {
        when(jobNodeStorage.isJobNodeExisted(ConfigurationNode.JOB_CLASS)).thenReturn(true);
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.JOB_CLASS)).thenReturn(TestJob.class.getCanonicalName());
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        configService.persistJobConfiguration();
        verifyPersistJobConfiguration();
    }
    
    private void verifyPersistJobConfiguration() {
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_CLASS, TestJob.class.getCanonicalName());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_TOTAL_COUNT, jobConfig.getShardingTotalCount());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.SHARDING_ITEM_PARAMETERS, jobConfig.getShardingItemParameters());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_PARAMETER, jobConfig.getJobParameter());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.CRON, jobConfig.getCron());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_EXECUTION, jobConfig.isMonitorExecution());
        if (DataFlowElasticJob.class.isAssignableFrom(jobConfig.getJobClass())) {
            DataFlowJobConfiguration dataFlowJobConfiguration = (DataFlowJobConfiguration) jobConfig;
            verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS, dataFlowJobConfiguration.getProcessCountIntervalSeconds());
            verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT, dataFlowJobConfiguration.getConcurrentDataProcessThreadCount());
            verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.STREAMING_PROCESS, dataFlowJobConfiguration.isStreamingProcess());
        }
        if (ScriptElasticJob.class.isAssignableFrom(jobConfig.getJobClass())) {
            ScriptJobConfiguration scriptJobConfiguration = (ScriptJobConfiguration) jobConfig;
            verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.SCRIPT_COMMAND_LINE, scriptJobConfiguration.getScriptCommandLine());
        }
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.MAX_TIME_DIFF_SECONDS, jobConfig.getMaxTimeDiffSeconds());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.FAILOVER, jobConfig.isFailover());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.MISFIRE, jobConfig.isMisfire());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS, jobConfig.getJobShardingStrategyClass());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.DESCRIPTION, jobConfig.getDescription());
        verify(jobNodeStorage).fillJobNodeIfNullOrOverwrite(ConfigurationNode.MONITOR_PORT, jobConfig.getMonitorPort());
    }
    
    @Test
    public void assertGetShardingTotalCount() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_TOTAL_COUNT)).thenReturn("3");
        assertThat(configService.getShardingTotalCount(), is(3));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_TOTAL_COUNT);
    }
    
    @Test
    public void assertGetShardingTotalCountWhenNodeIsNotExisted() {
        assertThat(configService.getShardingTotalCount(), is(-1));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_TOTAL_COUNT);
    }
    
    @Test
    public void assertGetShardingItemParametersWhenIsEmpty() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS)).thenReturn("");
        assertThat(configService.getShardingItemParameters(), is(Collections.EMPTY_MAP));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS);
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenPairFormatInvalid() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS)).thenReturn("xxx-xxx");
        try {
            configService.getShardingItemParameters();
        } finally {
            verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS);
        }
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenItemIsNotNumber() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS)).thenReturn("xxx=xxx");
        try {
            configService.getShardingItemParameters();
        } finally {
            verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS);
        }
    }
    
    @Test
    public void assertGetShardingItemParameters() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS)).thenReturn("0=A,1=B,2=C");
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(configService.getShardingItemParameters(), is(expected));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.SHARDING_ITEM_PARAMETERS);
    }
    
    @Test
    public void assertGetJobParameter() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.JOB_PARAMETER)).thenReturn("para");
        assertThat(configService.getJobParameter(), is("para"));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.JOB_PARAMETER);
    }
    
    @Test
    public void assertGetCron() {
        when(jobNodeStorage.getJobNodeDataDirectly(ConfigurationNode.CRON)).thenReturn("0/1 * * * * ?");
        assertThat(configService.getCron(), is("0/1 * * * * ?"));
        verify(jobNodeStorage).getJobNodeDataDirectly(ConfigurationNode.CRON);
    }
    
    @Test
    public void assertIsMonitorExecution() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_EXECUTION)).thenReturn("true");
        assertTrue(configService.isMonitorExecution());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MONITOR_EXECUTION);
    }
    
    @Test
    public void assertGetProcessCountIntervalSeconds() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS)).thenReturn("300");
        assertThat(configService.getProcessCountIntervalSeconds(), is(300));
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.PROCESS_COUNT_INTERVAL_SECONDS);
    }
    
    @Test
    public void assertGetConcurrentDataProcessThreadCount() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT)).thenReturn("1");
        assertThat(configService.getConcurrentDataProcessThreadCount(), is(1));
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.CONCURRENT_DATA_PROCESS_THREAD_COUNT);
    }
    
    @Test
    public void assertIsNotStreamingProcess() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.STREAMING_PROCESS)).thenReturn("false");
        assertFalse(configService.isStreamingProcess());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.STREAMING_PROCESS);
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS)).thenReturn("-1");
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS);
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS)).thenReturn("60");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(System.currentTimeMillis());
        configService.checkMaxTimeDiffSecondsTolerable();
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS);
        verify(jobNodeStorage).getRegistryCenterTime();
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS)).thenReturn("60");
        when(jobNodeStorage.getRegistryCenterTime()).thenReturn(0L);
        try {
            configService.checkMaxTimeDiffSecondsTolerable();
        } finally {
            verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MAX_TIME_DIFF_SECONDS);
            verify(jobNodeStorage).getRegistryCenterTime();
        }
    }
    
    @Test
    public void assertIsNotFailoverWhenNotMonitorExecution() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_EXECUTION)).thenReturn("false");
        assertFalse(configService.isFailover());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MONITOR_EXECUTION);
    }
    
    @Test
    public void assertIsNotFailoverWhenMonitorExecution() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_EXECUTION)).thenReturn("true");
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.FAILOVER)).thenReturn("false");
        assertFalse(configService.isFailover());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MONITOR_EXECUTION);
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.FAILOVER);
    }
    
    @Test
    public void assertIsFailover() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_EXECUTION)).thenReturn("true");
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.FAILOVER)).thenReturn("true");
        assertTrue(configService.isFailover());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MONITOR_EXECUTION);
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.FAILOVER);
    }
    
    @Test
    public void assertIsMisfire() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MISFIRE)).thenReturn("true");
        assertTrue(configService.isMisfire());
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MISFIRE);
    }
    
    @Test
    public void assertGetJobShardingStrategyClass() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS)).thenReturn(JobShardingStrategy.class.getName());
        assertThat(configService.getJobShardingStrategyClass(), is(JobShardingStrategy.class.getName()));
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.JOB_SHARDING_STRATEGY_CLASS);
    }
    
    @Test
    public void assertGetMonitorPort() {
        when(jobNodeStorage.getJobNodeData(ConfigurationNode.MONITOR_PORT)).thenReturn("8888");
        assertThat(configService.getMonitorPort(), is(8888));
        verify(jobNodeStorage).getJobNodeData(ConfigurationNode.MONITOR_PORT);
    }
    
    @Test
    public void assertGetJobName() {
        when(jobNodeStorage.getJobConfiguration()).thenReturn(jobConfig);
        assertThat(configService.getJobName(), is("testJob"));
        verify(jobNodeStorage).getJobConfiguration();
    }
}
