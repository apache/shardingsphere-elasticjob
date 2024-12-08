package org.apache.shardingsphere.elasticjob.kernel.internal.sharding.strategy.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.kernel.internal.sharding.strategy.JobShardingStrategy;

/**
 * Single sharding Balance strategy, referenced of ROUND_ROBIN strategy.
 * <pre>
 * it resolves the problem which ROUND_ROBIN is stick with the certain one job instance
 * for the hashcode of job name is a constant value. while with SINGLE_SHARDING_BALANCE, it allows
 * the job running on all the job instances each one by one, just like loop the job instances.
 *
 * this is the real round robin balance job running in the job instance dimension.
 * </pre>
 *
 * @author hongzhu
 * @version V1.0
 * @since 2024-12-03 19:19
 */
public class SingleShardingBalanceJobShardingStrategy implements JobShardingStrategy {

    private final AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();

    @Override
    public Map<JobInstance, List<Integer>> sharding(final List<JobInstance> jobInstances, final String jobName, final int shardingTotalCount) {
        int shardingUnitsSize = jobInstances.size();
        int offset = Math.abs(jobName.hashCode() + ((Long)System.currentTimeMillis()).intValue()) % shardingUnitsSize;

        List<JobInstance> result = new ArrayList<>(shardingUnitsSize);
        for (int i = 0; i < shardingUnitsSize; i++) {
            int index = (i + offset) % shardingUnitsSize;
            result.add(jobInstances.get(index));
        }

        return averageAllocationJobShardingStrategy.sharding(result, jobName, shardingTotalCount);
    }

    @Override
    public String getType() {
        return "SINGLE_SHARDING_BALANCE";
    }

}
