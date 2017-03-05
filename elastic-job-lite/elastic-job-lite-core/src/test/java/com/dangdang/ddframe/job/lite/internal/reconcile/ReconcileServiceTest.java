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

package com.dangdang.ddframe.job.lite.internal.reconcile;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.fixture.TestSimpleJob;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

public class ReconcileServiceTest {
    
    @Mock
    private ConfigurationService configService;
    
    @Mock
    private ShardingService shardingService;
    
    @Mock
    private LeaderElectionService leaderElectionService;
    
    private final ReconcileService reconcileService = new ReconcileService(null, "job_test");
    
    @Before
    public void setup() throws NoSuchFieldException {
        MockitoAnnotations.initMocks(this);
        ReflectionUtils.setFieldValue(reconcileService, "lastReconcileTime", 1L);
        ReflectionUtils.setFieldValue(reconcileService, "configService", configService);
        ReflectionUtils.setFieldValue(reconcileService, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(reconcileService, "leaderElectionService", leaderElectionService);
    }
    
    @Test
    public void assertReconcile() throws Exception {
        Mockito.when(configService.load(true)).thenReturn(LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(JobCoreConfiguration.newBuilder("test_job", "0/1 * * * * ?", 3).build(),
                TestSimpleJob.class.getCanonicalName())).reconcileIntervalMinutes(1).build());
        Mockito.when(shardingService.isNeedSharding()).thenReturn(false);
        Mockito.when(shardingService.hasNotRunningShardingNode()).thenReturn(true);
        Mockito.when(leaderElectionService.isLeader()).thenReturn(true);
        reconcileService.runOneIteration();
        Mockito.verify(shardingService).isNeedSharding();
        Mockito.verify(shardingService).hasNotRunningShardingNode();
        Mockito.verify(shardingService).setReshardingFlag();
        Mockito.verify(leaderElectionService).isLeader();
    }
}
