package com.dangdang.ddframe.job.lite.internal.worker.reconcile;

import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.worker.AbstractWorker;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 修复作业服务器中不正常的状态.
 * 
 * @author qianzhiqiang
 *
 */
@Slf4j
public class ReconcileWorker extends AbstractWorker {
    
    public final static int DEFAULT_SLEEP_TIME = 60000;
    
    @Setter
    private static volatile int sleepTime = 60000;
    
    private static volatile boolean isContinued = true;
    
    private final ShardingService shardingService;
    
    private final LeaderElectionService leaderElectionService;
    
    public ReconcileWorker(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.shardingService = new ShardingService(regCenter, jobName);
        this.leaderElectionService = new LeaderElectionService(regCenter, jobName);
    }
    
    private void doReconcile() {
        log.debug("Reconcile starting!");
        if (!shardingService.isNeedSharding()
                && shardingService.hasNotRunningShardingNode()) {
            shardingService.setReshardingFlag();
        }
    }
    
    /**
     * 作业执行方法.
     * 
     * <p>
     * leader作业服务器去进行所有作业服务器的判断。
     * 如果休眠时间为负数，则不进行
     * </p>
     */
    @Override
    public void doWork() {
        while (isContinued) {
            if (leaderElectionService.isLeader() && sleepTime > 0) {
                doReconcile();
            }
            log.debug("Sleep {}ms", sleepTime);
            BlockUtils.sleep(sleepTime > 0 ? sleepTime : DEFAULT_SLEEP_TIME);
        }
    }
}
