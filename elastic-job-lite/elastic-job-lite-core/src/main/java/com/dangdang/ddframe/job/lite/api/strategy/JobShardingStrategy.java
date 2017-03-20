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

import java.util.Collection;
import java.util.List;

/**
 * 作业分片策略.
 * 
 * @author zhangliang
 */
public interface JobShardingStrategy {
    
    /**
     * 作业分片.
     * 
     * @param jobShardingUnits 所有参与分片的单元列表
     * @param jobShardingMetadata 作业分片策略选项
     * @return 分片结果集合
     */
    Collection<JobShardingResult> sharding(List<JobShardingUnit> jobShardingUnits, JobShardingMetadata jobShardingMetadata);
}
