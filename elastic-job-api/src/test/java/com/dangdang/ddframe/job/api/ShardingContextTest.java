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

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ShardingContextTest {
    
    @Test
    public void assertGetShardingContext() {
        ShardingContext actual = createShardingContext();
        ShardingContext expected = actual.getShardingContext(0);
        assertThat(actual.getJobName(), is(expected.getJobName()));
        assertThat(actual.getShardingTotalCount(), is(expected.getShardingTotalCount()));
        assertThat(actual.getJobParameter(), is(expected.getJobParameter()));
        assertThat(expected.getShardingItems().size(), is(1));
        assertThat(expected.getShardingItems().get(0).getItem(), is(0));
        assertThat(expected.getShardingItems().get(0).getParameter(), is("sharding_param_0"));
        assertThat(expected.getShardingItems().get(0).getOffset(), is(""));
    }
    
    @Test
    public void assertToJson() {
        assertThat(createShardingContext().toJson(), is("{\"jobName\":\"test_job\",\"shardingTotalCount\":10,\"jobParameter\":\"job_param\","
                + "\"shardingItems\":{\"0\":{\"item\":0,\"parameter\":\"sharding_param_0\",\"offset\":\"\"},\"1\":{\"item\":1,\"parameter\":\"sharding_param_1\",\"offset\":\"\"}}}"));
    }
    
    private ShardingContext createShardingContext() {
        return new ShardingContext("test_job", 10, "job_param", 
                Arrays.asList(new ShardingContext.ShardingItem(0, "sharding_param_0", ""), new ShardingContext.ShardingItem(1, "sharding_param_1", "")));
    }
}
