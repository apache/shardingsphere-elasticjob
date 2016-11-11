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

package com.dangdang.ddframe.job.lite.internal.schedule;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestDataflowJob;
import com.dangdang.ddframe.job.lite.fixture.util.JobConfigurationUtil;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.lite.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulerFacadeTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    @Mock
    private ServerService serverService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private ListenerManager listenerManager;
    
    private final LiteJobConfiguration liteJobConfig = JobConfigurationUtil.createDataflowLiteJobConfiguration();
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        schedulerFacade = new SchedulerFacade(null, "test_job", Collections.<ElasticJobListener>emptyList());
        when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), false)).build());
        ReflectionUtils.setFieldValue(schedulerFacade, "configService", configService);
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(schedulerFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "monitorService", monitorService);
        ReflectionUtils.setFieldValue(schedulerFacade, "listenerManager", listenerManager);
    }
    
    @Test
    public void assertClearPreviousServerStatus() {
        schedulerFacade.clearPreviousServerStatus();
        verify(serverService).clearPreviousServerStatus();
    }
    
    @Test
    public void assertRegisterStartUpInfo() {
        when(configService.load(false)).thenReturn(LiteJobConfiguration.newBuilder(new DataflowJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestDataflowJob.class.getCanonicalName(), false)).build());
        schedulerFacade.registerStartUpInfo(liteJobConfig);
        verify(listenerManager).startAllListeners();
        verify(leaderElectionService).leaderForceElection();
        verify(configService).persist(liteJobConfig);
        verify(serverService).persistServerOnline(true);
        verify(serverService).clearJobPausedStatus();
        verify(shardingService).setReshardingFlag();
        verify(monitorService).listen();
        verify(configService).load(false);
        verify(listenerManager).setCurrentShardingTotalCount(3);
    }
    
    @Test
    public void assertReleaseJobResource() {
        schedulerFacade.releaseJobResource();
        verify(monitorService).close();
        verify(serverService).removeServerStatus();
    }
    
    @Test
    public void assertLoadJobConfiguration() {
        LiteJobConfiguration expected = LiteJobConfiguration.newBuilder(null).build();
        when(configService.load(false)).thenReturn(expected);
        assertThat(schedulerFacade.loadJobConfiguration(), is(expected));
    }
    
    @Test
    public void assertNewJobTriggerListener() {
        assertThat(schedulerFacade.newJobTriggerListener(), instanceOf(JobTriggerListener.class));
    }
}
