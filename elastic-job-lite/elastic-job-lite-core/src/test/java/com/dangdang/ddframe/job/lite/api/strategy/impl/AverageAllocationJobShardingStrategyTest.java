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

package com.dangdang.ddframe.job.lite.api.strategy.impl;

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AverageAllocationJobShardingStrategyTest {
    
    private final JobShardingStrategy jobShardingStrategy = new AverageAllocationJobShardingStrategy();
    
    @Test
    public void shardingForZeroServer() {
        assertThat(jobShardingStrategy.sharding(Collections.<String>emptyList(), getJobShardingStrategyOption(3)), is(Collections.EMPTY_MAP));
    }
    
    @Test
    public void shardingForOneServer() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(1);
        expected.put("host0", Arrays.asList(0, 1, 2));
        assertThat(jobShardingStrategy.sharding(Collections.singletonList("host0"), getJobShardingStrategyOption(3)), is(expected));
    }
    
    @Test
    public void shardingForServersMoreThanShardingCount() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Collections.singletonList(0));
        expected.put("host1", Collections.singletonList(1));
        expected.put("host2", Collections.<Integer>emptyList());
        assertThat(jobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), getJobShardingStrategyOption(2)), is(expected));
    }
    
    @Test
    public void shardingForServersLessThanShardingCountAliquot() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(0, 1, 2));
        expected.put("host1", Arrays.asList(3, 4, 5));
        expected.put("host2", Arrays.asList(6, 7, 8));
        assertThat(jobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), getJobShardingStrategyOption(9)), is(expected));
    }
    
    @Test
    public void shardingForServersLessThanShardingCountAliquantFor8ShardingCountAnd3Servers() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(0, 1, 6));
        expected.put("host1", Arrays.asList(2, 3, 7));
        expected.put("host2", Arrays.asList(4, 5));
        assertThat(jobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), getJobShardingStrategyOption(8)), is(expected));
    }
    
    @Test
    public void shardingForServersLessThanShardingCountAliquantFor10ShardingCountAnd3Servers() {
        Map<String, List<Integer>> expected = new LinkedHashMap<>(3);
        expected.put("host0", Arrays.asList(0, 1, 2, 9));
        expected.put("host1", Arrays.asList(3, 4, 5));
        expected.put("host2", Arrays.asList(6, 7, 8));
        assertThat(jobShardingStrategy.sharding(Arrays.asList("host0", "host1", "host2"), getJobShardingStrategyOption(10)), is(expected));
    }
    
    private JobShardingStrategyOption getJobShardingStrategyOption(final int shardingTotalCount) {
        return new JobShardingStrategyOption("test_job", shardingTotalCount);
    }
}
