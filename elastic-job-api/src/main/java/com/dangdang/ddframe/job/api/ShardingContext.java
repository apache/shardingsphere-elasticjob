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

import com.dangdang.ddframe.job.util.json.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分片上下文.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class ShardingContext {
    
    /**
     * 作业名称.
     */
    private String jobName;
    
    /**
     * 分片总数.
     */
    private int shardingTotalCount;
    
    /**
     * 作业自定义参数.
     * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
     */
    private String jobParameter;
    
    /**
     * 分配于本作业实例的分片项.
     */
    private final Map<Integer, ShardingItem> shardingItems;
    
    public ShardingContext(final String jobName, final int shardingTotalCount, final String jobParameter, final Collection<ShardingItem> shardingItems) {
        this.jobName = jobName;
        this.shardingTotalCount = shardingTotalCount;
        this.jobParameter = jobParameter;
        this.shardingItems = new LinkedHashMap<>(shardingTotalCount);
        for (ShardingItem each : shardingItems) {
            this.shardingItems.put(each.getItem(), each);
        }
    }
    
    /**
     * 根据分片项生成分片上下文.
     * 
     * @param shardingItem 分片项
     * @return 分片上下文
     */
    public ShardingContext getShardingContext(final int shardingItem) {
        return new ShardingContext(jobName, shardingTotalCount, jobParameter, Collections.singletonList(shardingItems.get(shardingItem)));
    }
    
    /**
     * 获取Json格式字符串.
     *
     * @return Json格式字符串
     */
    public String toJson() {
        return GsonFactory.getGson().toJson(this);
    }
    
    /**
     * 分片项.
     */
    @AllArgsConstructor
    @Getter
    @ToString
    public static final class ShardingItem {
        
        /**
         * 运行在本作业服务器的分片序列号.
         */
        private final int item;
        
        /**
         * 分片序列号的个性化参数.
         */
        private final String parameter;
    }
}
