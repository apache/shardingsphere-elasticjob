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

package com.dangdang.ddframe.job.lite.internal.sharding;

import com.dangdang.ddframe.job.lite.internal.election.ElectionNode;
import com.dangdang.ddframe.job.lite.internal.server.ServerNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Elastic Job分片节点名称的常量类.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ShardingNode {
    
    static final String LEADER_SHARDING_ROOT = ElectionNode.ROOT + "/sharding";
    
    static final String NECESSARY = LEADER_SHARDING_ROOT + "/necessary";
    
    static final String PROCESSING = LEADER_SHARDING_ROOT + "/processing";
    
    private static final String SERVER_SHARDING = ServerNode.ROOT + "/%s/sharding";
    
    static String getShardingNode(final String ip) {
        return String.format(SERVER_SHARDING, ip);
    }
}
