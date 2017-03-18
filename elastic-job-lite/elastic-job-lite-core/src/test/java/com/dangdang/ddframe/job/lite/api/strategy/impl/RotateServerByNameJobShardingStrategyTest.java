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

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingResult;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingUnit;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RotateServerByNameJobShardingStrategyTest {
    
    private RotateServerByNameJobShardingStrategy rotateServerByNameJobShardingStrategy = new RotateServerByNameJobShardingStrategy();
    
    @Test
    public void assertSharding1() {
        Collection<JobShardingResult> expected = Arrays.asList(
                new JobShardingResult(new JobShardingUnit("host1", "test_job_instance_id"), Collections.singletonList(0)),
                new JobShardingResult(new JobShardingUnit("host2", "test_job_instance_id"), Collections.singletonList(1)),
                new JobShardingResult(new JobShardingUnit("host0", "test_job_instance_id"), Collections.<Integer>emptyList()));
        assertThat(rotateServerByNameJobShardingStrategy.sharding(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id"), new JobShardingUnit("host1", "test_job_instance_id"), new JobShardingUnit("host2", "test_job_instance_id")), 
                new JobShardingStrategyOption("1", 2)), is(expected));
    }
    
    @Test
    public void assertSharding2() {
        Collection<JobShardingResult> expected = Arrays.asList(
                new JobShardingResult(new JobShardingUnit("host2", "test_job_instance_id"), Collections.singletonList(0)),
                new JobShardingResult(new JobShardingUnit("host0", "test_job_instance_id"), Collections.singletonList(1)),
                new JobShardingResult(new JobShardingUnit("host1", "test_job_instance_id"), Collections.<Integer>emptyList()));
        assertThat(rotateServerByNameJobShardingStrategy.sharding(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id"), new JobShardingUnit("host1", "test_job_instance_id"), new JobShardingUnit("host2", "test_job_instance_id")),
                new JobShardingStrategyOption("2", 2)), is(expected));
    }
    
    @Test
    public void assertSharding3() {
        Collection<JobShardingResult> expected = Arrays.asList(
                new JobShardingResult(new JobShardingUnit("host0", "test_job_instance_id"), Collections.singletonList(0)),
                new JobShardingResult(new JobShardingUnit("host1", "test_job_instance_id"), Collections.singletonList(1)),
                new JobShardingResult(new JobShardingUnit("host2", "test_job_instance_id"), Collections.<Integer>emptyList()));
        assertThat(rotateServerByNameJobShardingStrategy.sharding(Arrays.asList(
                new JobShardingUnit("host0", "test_job_instance_id"), new JobShardingUnit("host1", "test_job_instance_id"), new JobShardingUnit("host2", "test_job_instance_id")),
                new JobShardingStrategyOption("3", 2)), is(expected));
    }
}
