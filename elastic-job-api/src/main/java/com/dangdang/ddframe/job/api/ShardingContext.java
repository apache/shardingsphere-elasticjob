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

package com.dangdang.ddframe.job.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 分片上下文.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingContext implements Serializable {
    
    private static final long serialVersionUID = -4585977349142082152L;
    
    /**
     * 作业名称.
     */
    private final String jobName;
    
    /**
     * 分片总数.
     */
    private final int shardingTotalCount;
    
    /**
     * 作业自定义参数.
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     */
    private final String jobParameter;
    
    /**
     * 分配于本作业实例的分片项.
     */
    private final Map<Integer, String> shardingItemParameters;
    
    /**
     * 根据分片项生成分片上下文.
     * 
     * @param shardingItem 分片项
     * @return 分片上下文
     */
    public ShardingContext getShardingContext(final int shardingItem) {
        Map<Integer, String> shardingItemParameters = new HashMap<>(1, 1);
        shardingItemParameters.put(shardingItem, this.shardingItemParameters.get(shardingItem));
        return new ShardingContext(jobName, shardingTotalCount, jobParameter, shardingItemParameters);
    }
}
