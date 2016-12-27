package com.dangdang.ddframe.job.lite.internal.reconcile;

import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.execution.ExecutionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 修复作业中不正常的状态
 * 
 * @author qianzhiqiang
 *
 */
@Slf4j
public class ReconcileWorker implements Runnable {
    
    private final ShardingService shardingService;
    
    private final LeaderElectionService leaderElectionService;
    
    private final ExecutionService executionService;
    
    private final long sleepTime = 5000L;
    
    private static volatile boolean isContinued = true;
	
    public ReconcileWorker(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.shardingService = new ShardingService(regCenter, jobName);
        this.leaderElectionService = new LeaderElectionService(regCenter, jobName);
        this.executionService = new ExecutionService(regCenter, jobName);
    }
	
	/**
	 * 查询所有的作业服务器是否是错误状态，如果有则置为重分片状态
	 */
    public void doReconcile() {
        log.debug("Reconcile starting!");
        if(!shardingService.isNeedSharding()
                && shardingService.isNoRunningButContainShardingNode()) {
            executionService.setNeedFixExecutionInfoFlag();
        }
    }
	
    @Override
    public void run() {
        while (isContinued) {
            if(leaderElectionService.isLeader()) {
                doReconcile();
            }
            log.debug("Sleep {}ms", sleepTime);
            BlockUtils.sleep(sleepTime);
        }
    }
	
	/**
	 * 停止本作业
	 */
    public static void stop() {
        isContinued = false;
    }
}
