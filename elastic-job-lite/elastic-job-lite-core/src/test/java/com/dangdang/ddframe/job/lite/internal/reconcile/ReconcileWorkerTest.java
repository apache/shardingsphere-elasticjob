package com.dangdang.ddframe.job.lite.internal.reconcile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.unitils.util.ReflectionUtils;

import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;

public final class ReconcileWorkerTest {
    
    @Mock
    public ShardingService shardingService;
	
    @Mock
    public ExecutionService executionService;
	
    @Mock
    public LeaderElectionService leaderElectionService;
	
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
	
    @Test
    public void assertReconcileWorker() throws NoSuchFieldException {
        ReconcileWorker worker = new ReconcileWorker(null, "test-job");
        Mockito.when(shardingService.isNeedSharding()).thenReturn(false);
        Mockito.when(shardingService.isNoRunningButContainShardingNode()).thenReturn(true);
        Mockito.when(leaderElectionService.isLeader()).thenReturn(true);
        ReflectionUtils.setFieldValue(worker, "shardingService", shardingService);
        ReflectionUtils.setFieldValue(worker, "executionService", executionService);
        ReflectionUtils.setFieldValue(worker, "leaderElectionService", leaderElectionService);
        new Thread(worker).start();
        BlockUtils.sleep(100);
        Mockito.verify(shardingService, Mockito.atLeastOnce()).isNoRunningButContainShardingNode();
        Mockito.verify(executionService, Mockito.atLeastOnce()).setNeedFixExecutionInfoFlag();
        Mockito.verify(leaderElectionService, Mockito.atLeastOnce()).isLeader();
        ReconcileWorker.stop();
    }
}
