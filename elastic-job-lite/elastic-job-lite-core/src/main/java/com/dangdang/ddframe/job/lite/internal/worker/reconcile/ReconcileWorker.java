package com.dangdang.ddframe.job.lite.internal.worker.reconcile;

import com.dangdang.ddframe.job.lite.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingService;
import com.dangdang.ddframe.job.lite.internal.worker.AbstractWorker;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.concurrent.BlockUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 修复作业服务器中不正常的状态
 * 
 * @author qianzhiqiang
 *
 */
@Slf4j
public class ReconcileWorker extends AbstractWorker {

    public final static long DEFAULT_SLEEP_TIME = 60000L;
    
    private final ShardingService shardingService;
    
    private final LeaderElectionService leaderElectionService;
    
    private static volatile boolean isContinued = true;

    @Setter
    private static volatile long sleepTime = DEFAULT_SLEEP_TIME;
	
    public ReconcileWorker(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.shardingService = new ShardingService(regCenter, jobName);
        this.leaderElectionService = new LeaderElectionService(regCenter, jobName);
    }
	
	/**
	 * 查询所有的作业服务器是否是错误状态，如果有则置为重分片状态
	 */
    public void doReconcile() {
        log.debug("Reconcile starting!");
        if(!shardingService.isNeedSharding()
                && shardingService.isNoRunningButContainShardingNode()) {
            shardingService.setReshardingFlag();
        }
    }

	/**
	 * 停止本作业
	 */
    public static void stop() {
        isContinued = false;
    }

    /**
     * 作业执行方法
     * 
     * <p>
     * leader作业服务器去进行所有作业服务器的判断。
     * 如果休眠时间为负数，则不进行
     * </p>
     */
    @Override
    public void doWork() {
        while (isContinued) {
            if(leaderElectionService.isLeader() && sleepTime > 0) {
                doReconcile();
            }
            log.debug("Sleep {}ms", sleepTime);
            BlockUtils.sleep(sleepTime > 0 ? sleepTime : DEFAULT_SLEEP_TIME);
        }
    }
}
