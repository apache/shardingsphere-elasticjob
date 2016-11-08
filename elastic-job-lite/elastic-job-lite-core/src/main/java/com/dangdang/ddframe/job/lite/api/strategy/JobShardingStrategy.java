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

package com.dangdang.ddframe.job.lite.api.strategy;

import java.util.List;
import java.util.Map;

/**
 * 作业分片策略.
 * 
 * @author zhangliang
 */
public interface JobShardingStrategy {
    
    /**
     * 进行作业分片.
     * 
     * @param serversList 所有参与分片的服务器列表
     * @param option 作业分片策略选项
     * @return 分配分片的服务器IP和分片集合的映射
     */
    Map<String, List<Integer>> sharding(List<String> serversList, JobShardingStrategyOption option);
}
