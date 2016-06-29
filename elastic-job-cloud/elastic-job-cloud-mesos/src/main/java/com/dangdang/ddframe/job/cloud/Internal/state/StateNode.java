/*
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

package com.dangdang.ddframe.job.cloud.Internal.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 云作业状态根节点常量类.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateNode {
    
    private static final String ROOT = "/jobs/%s/state";
    
    private static final String SHARDING_ITEM = ROOT + "/%d";
    
    static final String RUNNING = SHARDING_ITEM + "/running";
    
    static final String PAUSED = SHARDING_ITEM + "/paused";
    
    static final String SHUTDOWN = SHARDING_ITEM + "/shutdown";
    
    static String getRootNodePath(final String jobName) {
        return String.format(ROOT, jobName);
    }
    
    static String getShardingItemNodePath(final String jobName, final int shardingItem) {
        return String.format(SHARDING_ITEM, jobName, shardingItem);
    }
    
    static String getRunningNodePath(final String jobName, final int shardingItem) {
        return String.format(RUNNING, jobName, shardingItem);
    }
    
    static String getPausedNodePath(final String jobName, final int shardingItem) {
        return String.format(PAUSED, jobName, shardingItem);
    }
    
    static String getShutdownNodePath(final String jobName, final int shardingItem) {
        return String.format(SHUTDOWN, jobName, shardingItem);
    }
}
