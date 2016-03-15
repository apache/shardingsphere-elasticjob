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

package com.dangdang.ddframe.job.internal.schedule;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.fixture.TestJob;
import com.dangdang.ddframe.job.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.execution.ExecutionContextService;
import com.dangdang.ddframe.job.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.internal.failover.FailoverService;
import com.dangdang.ddframe.job.internal.listener.ListenerManager;
import com.dangdang.ddframe.job.internal.monitor.MonitorService;
import com.dangdang.ddframe.job.internal.offset.OffsetService;
import com.dangdang.ddframe.job.internal.server.ServerService;
import com.dangdang.ddframe.job.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.internal.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
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
    private ExecutionContextService executionContextService;
    
    @Mock
    private ExecutionService executionService;
    
    @Mock
    private FailoverService failoverService;
    
    @Mock
    private StatisticsService statisticsService;
    
    @Mock
    private OffsetService offsetService;
    
    @Mock
    private MonitorService monitorService;
    
    @Mock
    private ListenerManager listenerManager;
    
    @Mock
    private Caller caller;
    
    private JobConfiguration jobConfig = new JobConfiguration("testJob", TestJob.class, 3, "0/1 * * * * ?");
    
    private SchedulerFacade schedulerFacade;
    
    @Before
    public void setUp() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        schedulerFacade = new SchedulerFacade(null, jobConfig, Collections.<ElasticJobListener> singletonList(new TestElasticJobListener(caller)));
        ReflectionUtils.setFieldValue(schedulerFacade, "configService", configService);
        ReflectionUtils.setFieldValue(schedulerFacade, "leaderElectionService", leaderElectionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "serverService", serverService);
        ReflectionUtils.setFieldValue(schedulerFacade, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(schedulerFacade, "executionContextService", executionContextService);
        ReflectionUtils.setFieldValue(schedulerFacade, "executionService", executionService);
        ReflectionUtils.setFieldValue(schedulerFacade, "failoverService", failoverService);
        ReflectionUtils.setFieldValue(schedulerFacade, "statisticsService", statisticsService);
        ReflectionUtils.setFieldValue(schedulerFacade, "offsetService", offsetService);
        ReflectionUtils.setFieldValue(schedulerFacade, "monitorService", monitorService);
        ReflectionUtils.setFieldValue(schedulerFacade, "listenerManager", listenerManager);
        
    }
    
    @Test
    public void testNew() throws NoSuchFieldException {
        SchedulerFacade schedulerFacade = new SchedulerFacade(null, jobConfig, Arrays.asList(new TestElasticJobListener(caller), new TestDistributeOnceElasticJobListener()));
        List<ElasticJobListener> actual = ReflectionUtils.getFieldValue(schedulerFacade, ReflectionUtils.getFieldWithName(SchedulerFacade.class, "elasticJobListeners", false));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), instanceOf(TestElasticJobListener.class));
        assertThat(actual.get(1), instanceOf(TestDistributeOnceElasticJobListener.class));
        assertNotNull(ReflectionUtils.getFieldValue(actual.get(1), AbstractDistributeOnceElasticJobListener.class.getDeclaredField("guaranteeService")));
    }
    
    @Test
    public void testRegisterStartUpInfo() {
        schedulerFacade.registerStartUpInfo();
        verify(listenerManager).startAllListeners();
        verify(leaderElectionService).leaderElection();
        verify(configService).persistJobConfiguration();
        verify(serverService).persistServerOnline();
        verify(serverService).clearJobStoppedStatus();
        verify(statisticsService).startProcessCountJob();
        verify(shardingService).setReshardingFlag();
        verify(monitorService).listen();
    }
    
    @Test
    public void testReleaseJobResource() {
        schedulerFacade.releaseJobResource();
        verify(monitorService).close();
        verify(statisticsService).stopProcessCountJob();
    }
    
    @Test
    public void testResumeCrashedJobInfo() {
        when(shardingService.getLocalHostShardingItems()).thenReturn(Collections.<Integer>emptyList());
        schedulerFacade.resumeCrashedJobInfo();
        verify(serverService).persistServerOnline();
        verify(shardingService).getLocalHostShardingItems();
        verify(executionService).clearRunningInfo(Collections.<Integer>emptyList());
    }
    
    @Test
    public void testClearJobStoppedStatus() {
        schedulerFacade.clearJobStoppedStatus();
        verify(serverService).clearJobStoppedStatus();
    }
    
    @Test
    public void testIsJobStoppedManually() {
        when(serverService.isJobStoppedManually()).thenReturn(true);
        assertTrue(schedulerFacade.isJobStoppedManually());
    }
    
    @Test
    public void testGetJobName() {
        when(configService.getJobName()).thenReturn("testJob");
        assertThat(schedulerFacade.getJobName(), is("testJob"));
    }
    
    @Test
    public void testGetCron() {
        when(configService.getCron()).thenReturn("0 * * * * *");
        assertThat(schedulerFacade.getCron(), is("0 * * * * *"));
    }
    
    @Test
    public void testIsMisfire() {
        when(configService.isMisfire()).thenReturn(true);
        assertTrue(schedulerFacade.isMisfire());
    }

    @Test
    public void testCheckMaxTimeDiffSecondsTolerable() {
        schedulerFacade.checkMaxTimeDiffSecondsTolerable();
        verify(configService).checkMaxTimeDiffSecondsTolerable();
    }
    
    @Test
    public void testNewJobTriggerListener() {
        assertThat(schedulerFacade.newJobTriggerListener(), instanceOf(JobTriggerListener.class));
    }
    
    @Test
    public void testFailoverIfUnnecessary() {
        when(configService.isFailover()).thenReturn(false);
        schedulerFacade.failoverIfNecessary(false);
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void testFailoverIfNecessaryButIsStopped() {
        when(configService.isFailover()).thenReturn(true);
        schedulerFacade.failoverIfNecessary(true);
        verify(failoverService, times(0)).failoverIfNecessary();
    }
    
    @Test
    public void testFailoverIfNecessary() {
        when(configService.isFailover()).thenReturn(true);
        schedulerFacade.failoverIfNecessary(false);
        verify(failoverService).failoverIfNecessary();
    }
    
    @Test
    public void testRegisterJobBegin() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        schedulerFacade.registerJobBegin(shardingContext);
        verify(executionService).registerJobBegin(shardingContext);
    }
    
    @Test
    public void testRegisterJobCompletedWhenFailoverDisabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(false);
        schedulerFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService, times(0)).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void testRegisterJobCompletedWhenFailoverEnabled() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(configService.isFailover()).thenReturn(true);
        schedulerFacade.registerJobCompleted(shardingContext);
        verify(executionService).registerJobCompleted(shardingContext);
        verify(failoverService).updateFailoverComplete(shardingContext.getShardingItems());
    }
    
    @Test
    public void testGetShardingContext() {
        JobExecutionMultipleShardingContext shardingContext = new JobExecutionMultipleShardingContext();
        when(executionContextService.getJobExecutionShardingContext()).thenReturn(shardingContext);
        assertThat(schedulerFacade.getShardingContext(), is(shardingContext));
        verify(shardingService).shardingIfNecessary();
    }
    
    @Test
    public void testMisfireIfNecessary() {
        when(executionService.misfireIfNecessary(Arrays.asList(0, 1))).thenReturn(true);
        assertThat(schedulerFacade.misfireIfNecessary(Arrays.asList(0, 1)), is(true));
    }
    
    @Test
    public void testClearMisfire() {
        schedulerFacade.clearMisfire(Arrays.asList(0, 1));
        verify(executionService).clearMisfire(Arrays.asList(0, 1));
    }
    
    @Test
    public void testIsNeedSharding() {
        when(shardingService.isNeedSharding()).thenReturn(true);
        assertThat(schedulerFacade.isNeedSharding(), is(true));
    }
    
    @Test
    public void testUpdateOffset() {
        schedulerFacade.updateOffset(0, "offset0");
        verify(offsetService).updateOffset(0, "offset0");
    }
    
    @Test
    public void testBeforeJobExecuted() {
        schedulerFacade.beforeJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).before();
    }
    
    @Test
    public void testAfterJobExecuted() {
        schedulerFacade.afterJobExecuted(new JobExecutionMultipleShardingContext());
        verify(caller).after();
    }
    
    @RequiredArgsConstructor
    static class TestElasticJobListener implements ElasticJobListener {
        
        private final Caller caller;
        
        @Override
        public void beforeJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
            caller.before();
        }
        
        @Override
        public void afterJobExecuted(final JobExecutionMultipleShardingContext shardingContext) {
            caller.after();
        }
    }
    
    static class TestDistributeOnceElasticJobListener extends AbstractDistributeOnceElasticJobListener {
        
        TestDistributeOnceElasticJobListener() {
            super(500000L, 500000L);
        }
        
        @Override
        public void doBeforeJobExecutedAtLastStarted(final JobExecutionMultipleShardingContext shardingContext) {
        }
        
        @Override
        public void doAfterJobExecutedAtLastCompleted(final JobExecutionMultipleShardingContext shardingContext) {
        }
    }
    
    interface Caller {
        
        void before();
        
        void after();
    }
}
