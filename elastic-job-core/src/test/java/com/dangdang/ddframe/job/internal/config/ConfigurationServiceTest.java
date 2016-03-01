/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.internal.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.exception.JobConflictException;
import com.dangdang.ddframe.job.exception.ShardingItemParametersException;
import com.dangdang.ddframe.job.exception.TimeDiffIntolerableException;
import com.dangdang.ddframe.job.internal.AbstractBaseJobTest;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategy;

public final class ConfigurationServiceTest extends AbstractBaseJobTest {
    
    private final ConfigurationService configService = new ConfigurationService(getRegistryCenter(), getJobConfig());
    
    @Test
    public void assertPersistNewJobConfiguration() {
        configService.persistJobConfiguration();
        assertJobConfiguration(getJobConfig());
    }
    
    @Test(expected = JobConflictException.class)
    public void assertPersistJobConfigurationForJobConflict() {
        configService.persistJobConfiguration();
        ConfigurationService configServiceForConflict = new ConfigurationService(getRegistryCenter(), new JobConfiguration("testJob", ConflictJob.class, 3, "0/1 * * * * ?"));
        configServiceForConflict.persistJobConfiguration();
    }
    
    @Test
    public void assertPersistJobConfigurationForOverwrite() throws Exception {
        configService.persistJobConfiguration();
        ConfigurationVersion originalConfigVersion = new ConfigurationVersion(getVersion("shardingTotalCount"), getVersion("shardingItemParameters"), getVersion("cron"));
        JobConfiguration jobConfig = createJobConfigurationForOverwrite();
        ConfigurationService configServiceForOverwrite = new ConfigurationService(getRegistryCenter(), jobConfig);
        configServiceForOverwrite.persistJobConfiguration();
        ConfigurationVersion targetConfigVersion = new ConfigurationVersion(getVersion("shardingTotalCount"), getVersion("shardingItemParameters"), getVersion("cron"));
        assertTrue(originalConfigVersion.getShardingTotalCountVersion() < targetConfigVersion.getShardingTotalCountVersion());
        assertTrue(originalConfigVersion.getShardingItemParametersVersion() < targetConfigVersion.getShardingItemParametersVersion());
        assertTrue(originalConfigVersion.getCronVersion() == targetConfigVersion.getCronVersion());
        assertJobConfiguration(jobConfig);
    }
    
    private JobConfiguration createJobConfigurationForOverwrite() {
        JobConfiguration result = new JobConfiguration("testJob", TestJob.class, 1, "0/1 * * * * ?");
        result.setShardingItemParameters("0=A");
        result.setOverwrite(true);
        return result;
    }
    
    @Test
    public void assertPersistJobConfigurationForNotOverwrite() throws Exception {
        configService.persistJobConfiguration();
        ConfigurationVersion originalConfigVersion = new ConfigurationVersion(getVersion("shardingTotalCount"), getVersion("shardingItemParameters"), getVersion("cron"));
        ConfigurationService configServiceForNotOverwrite = new ConfigurationService(getRegistryCenter(), createJobConfigurationForNotOverwrite());
        configServiceForNotOverwrite.persistJobConfiguration();
        ConfigurationVersion targetConfigVersion = new ConfigurationVersion(getVersion("shardingTotalCount"), getVersion("shardingItemParameters"), getVersion("cron"));
        assertTrue(originalConfigVersion.getShardingTotalCountVersion() == targetConfigVersion.getShardingTotalCountVersion());
        assertTrue(originalConfigVersion.getShardingItemParametersVersion() == targetConfigVersion.getShardingItemParametersVersion());
        assertTrue(originalConfigVersion.getCronVersion() == targetConfigVersion.getCronVersion());
        assertJobConfiguration(getJobConfig());
    }
    
    private JobConfiguration createJobConfigurationForNotOverwrite() {
        JobConfiguration result = createJobConfigurationForOverwrite();
        result.setOverwrite(false);
        return result;
    }
    
    private void assertJobConfiguration(final JobConfiguration jobConfiguration) {
        assertThat(getRegistryCenter().getDirectly("/testJob/config/jobClass"), is(jobConfiguration.getJobClass().getCanonicalName()));
        assertThat(Integer.parseInt(getRegistryCenter().getDirectly("/testJob/config/shardingTotalCount")), is(jobConfiguration.getShardingTotalCount()));
        assertThat(getRegistryCenter().getDirectly("/testJob/config/shardingItemParameters"), is(jobConfiguration.getShardingItemParameters()));
        assertThat(getRegistryCenter().getDirectly("/testJob/config/jobParameter"), is(jobConfiguration.getJobParameter()));
        assertThat(getRegistryCenter().getDirectly("/testJob/config/cron"), is(jobConfiguration.getCron()));
        assertThat(Boolean.valueOf(getRegistryCenter().getDirectly("/testJob/config/monitorExecution")), is(jobConfiguration.isMonitorExecution()));
        assertThat(Integer.parseInt(getRegistryCenter().getDirectly("/testJob/config/processCountIntervalSeconds")), is(jobConfiguration.getProcessCountIntervalSeconds()));
        assertThat(Integer.parseInt(getRegistryCenter().getDirectly("/testJob/config/concurrentDataProcessThreadCount")), is(jobConfiguration.getConcurrentDataProcessThreadCount()));
        assertThat(Integer.parseInt(getRegistryCenter().getDirectly("/testJob/config/fetchDataCount")), is(jobConfiguration.getFetchDataCount()));
        assertThat(Boolean.valueOf(getRegistryCenter().getDirectly("/testJob/config/failover")), is(jobConfiguration.isFailover()));
        assertThat(Boolean.valueOf(getRegistryCenter().getDirectly("/testJob/config/misfire")), is(jobConfiguration.isMisfire()));
        assertThat(getRegistryCenter().getDirectly("/testJob/config/jobShardingStrategyClass"), is(jobConfiguration.getJobShardingStrategyClass()));
        assertThat(getRegistryCenter().getDirectly("/testJob/config/description"), is(jobConfiguration.getDescription()));
    }
    
    private int getVersion(final String path) throws Exception {
        CuratorFramework client = (CuratorFramework) getRegistryCenter().getRawClient();
        Stat stat = new Stat();
        client.getZookeeperClient().getZooKeeper().getData("/zkRegTestCenter/testJob/config/" + path, false, stat);
        return stat.getVersion();
    }
    
    @Test
    public void assertGetShardingTotalCount() {
        configService.persistJobConfiguration();
        assertThat(configService.getShardingTotalCount(), is(3));
    }
    
    @Test
    public void assertGetShardingItemParametersWhenIsEmpty() {
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "");
        assertThat(configService.getShardingItemParameters(), is(Collections.EMPTY_MAP));
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenPairFormatError() {
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "xxx-xxx");
        configService.getShardingItemParameters();
    }
    
    @Test(expected = ShardingItemParametersException.class)
    public void assertGetShardingItemParametersWhenItemIsNotNumber() {
        getRegistryCenter().persist("/testJob/config/shardingItemParameters", "xxx=xxx");
        configService.getShardingItemParameters();
    }
    
    @Test
    public void assertGetShardingItemParameters() {
        configService.persistJobConfiguration();
        Map<Integer, String> expected = new HashMap<>(3);
        expected.put(0, "A");
        expected.put(1, "B");
        expected.put(2, "C");
        assertThat(configService.getShardingItemParameters(), is(expected));
    }
    
    @Test
    public void assertGetJobParameter() {
        configService.persistJobConfiguration();
        assertThat(configService.getJobParameter(), is("para"));
    }
    
    @Test
    public void assertGetCron() {
        configService.persistJobConfiguration();
        assertThat(configService.getCron(), is("0/1 * * * * ?"));
    }
    
    @Test
    public void assertIsMonitorExecution() {
        configService.persistJobConfiguration();
        assertTrue(configService.isMonitorExecution());
    }
    
    @Test
    public void assertGetProcessCountIntervalSeconds() {
        configService.persistJobConfiguration();
        assertThat(configService.getProcessCountIntervalSeconds(), is(300));
    }
    
    @Test
    public void assertGetConcurrentDataProcessThreadCount() {
        configService.persistJobConfiguration();
        assertThat(configService.getConcurrentDataProcessThreadCount(), is(1));
    }
    
    @Test
    public void assertGetFetchDataCount() {
        configService.persistJobConfiguration();
        assertThat(configService.getFetchDataCount(), is(1));
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerableWithDefaultValue() {
        configService.persistJobConfiguration();
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsMaxTimeDiffSecondsTolerable() {
        getJobConfig().setMaxTimeDiffSeconds(60);
        configService.persistJobConfiguration();
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test(expected = TimeDiffIntolerableException.class)
    public void assertIsNotMaxTimeDiffSecondsTolerable() {
        getJobConfig().setMaxTimeDiffSeconds(-60);
        configService.persistJobConfiguration();
        configService.checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void assertIsNotFailoverWhenNotMonitorExecution() {
        getJobConfig().setMonitorExecution(false);
        getJobConfig().setFailover(true);
        configService.persistJobConfiguration();
        assertFalse(configService.isFailover());
    }
    
    @Test
    public void assertIsNotFailoverWhenMonitorExecution() {
        configService.persistJobConfiguration();
        assertFalse(configService.isFailover());
    }
    
    @Test
    public void assertIsFailover() {
        getJobConfig().setFailover(true);
        configService.persistJobConfiguration();
        assertTrue(configService.isFailover());
    }
    
    @Test
    public void assertIsMisfire() {
        getJobConfig().setMisfire(true);
        configService.persistJobConfiguration();
        assertTrue(configService.isMisfire());
    }
    
    @Test
    public void assertGetJobShardingStrategyClass() {
        getJobConfig().setJobShardingStrategyClass(JobShardingStrategy.class.getName());
        configService.persistJobConfiguration();
        assertThat(configService.getJobShardingStrategyClass(), is(JobShardingStrategy.class.getName()));
    }
    
    class ConflictJob extends AbstractElasticJob {
        
        @Override
        protected void executeJob(final JobExecutionMultipleShardingContext jobExecutionShardingContext) {
        }

		@Override
		protected void afterAllShardingFinishedInternal() {
		}
    }
    
    @Getter
    @RequiredArgsConstructor
    class ConfigurationVersion {
        
        private final int shardingTotalCountVersion;
        
        private final int shardingItemParametersVersion;
        
        private final int cronVersion;
    }
}
