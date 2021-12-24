/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.infra.listener;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingContextsTest {
    
    @Test
    public void assertCreateShardingContext() {
        ShardingContexts shardingContexts = createShardingContexts();
        ShardingContext actual = shardingContexts.createShardingContext(1);
        assertThat(actual.getJobName(), is(shardingContexts.getJobName()));
        assertThat(actual.getTaskId(), is(shardingContexts.getTaskId()));
        assertThat(actual.getShardingTotalCount(), is(shardingContexts.getShardingTotalCount()));
        assertThat(actual.getJobParameter(), is(shardingContexts.getJobParameter()));
        assertThat(actual.getShardingItem(), is(1));
        assertThat(actual.getShardingParameter(), is(shardingContexts.getShardingItemParameters().get(1)));
    }
    
    private ShardingContexts createShardingContexts() {
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "A");
        map.put(1, "B");
        return new ShardingContexts("fake_task_id", "test_job", 2, "", map);
    }
}
