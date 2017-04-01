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

package com.dangdang.ddframe.job.lite.internal.failover;

import com.dangdang.ddframe.job.lite.internal.election.LeaderNode;
import com.dangdang.ddframe.job.lite.internal.sharding.ShardingNode;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;

/**
 * 失效转移节点路径.
 * 
 * @author zhangliang
 */
public final class FailoverNode {
    
    static final String FAILOVER = "failover";
    
    static final String LEADER_ROOT = LeaderNode.ROOT + "/" + FAILOVER;
    
    static final String ITEMS_ROOT = LEADER_ROOT + "/items";
    
    static final String ITEMS = ITEMS_ROOT + "/%s";
    
    static final String LATCH = LEADER_ROOT + "/latch";
    
    private static final String EXECUTION_FAILOVER = ShardingNode.ROOT + "/%s/" + FAILOVER;
    
    private final JobNodePath jobNodePath;
    
    public FailoverNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    static String getItemsNode(final int item) {
        return String.format(ITEMS, item);
    }
    
    static String getExecutionFailoverNode(final int item) {
        return String.format(EXECUTION_FAILOVER, item);
    }
    
    /**
     * 根据失效转移执行路径获取分片项.
     * 
     * @param path 失效转移执行路径
     * @return 分片项, 不是失效转移执行路径获则返回null
     */
    public Integer getItemByExecutionFailoverPath(final String path) {
        if (!isFailoverPath(path)) {
            return null;
        }
        return Integer.parseInt(path.substring(jobNodePath.getFullPath(ShardingNode.ROOT).length() + 1, path.lastIndexOf(FailoverNode.FAILOVER) - 1));
    }
    
    private boolean isFailoverPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ShardingNode.ROOT)) && path.endsWith(FailoverNode.FAILOVER);
    }
}
