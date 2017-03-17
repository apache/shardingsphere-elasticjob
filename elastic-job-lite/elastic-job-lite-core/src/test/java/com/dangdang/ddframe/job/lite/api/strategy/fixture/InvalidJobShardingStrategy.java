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

package com.dangdang.ddframe.job.lite.api.strategy.fixture;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingResult;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingUnit;

import java.util.Collection;
import java.util.List;

public class InvalidJobShardingStrategy implements JobShardingStrategy {
    
    public InvalidJobShardingStrategy(final String input) {
    }
    
    @Override
    public Collection<JobShardingResult> sharding(final List<JobShardingUnit> shardingUnits, final JobShardingStrategyOption option) {
        return null;
    }
}
