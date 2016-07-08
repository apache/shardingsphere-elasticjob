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

package com.dangdang.ddframe.job.cloud.context;

import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业运行上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class JobContext {
    
    private final CloudJobConfiguration jobConfig;
    
    private final List<Integer> assignedShardingItems;
    
    /**
     * 通过作业配置创建作业运行上下文.
     * 
     * @param jobConfig 作业配置
     * @return 作业运行上下文
     */
    public static JobContext from(final CloudJobConfiguration jobConfig) {
        int shardingTotalCount = jobConfig.getShardingTotalCount();
        List<Integer> shardingItems = new ArrayList<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            shardingItems.add(i);
        }
        return new JobContext(jobConfig, shardingItems);
    }
}
