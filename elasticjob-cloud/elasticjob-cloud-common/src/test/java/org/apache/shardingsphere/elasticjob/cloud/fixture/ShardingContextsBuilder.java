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

package org.apache.shardingsphere.elasticjob.cloud.fixture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingContextsBuilder {
    
    public static final String JOB_NAME = "test_job";

    /**
     * Get single sharding contexts.
     * @return ShardingContexts
     */
    public static ShardingContexts getSingleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        return new ShardingContexts("fake_task_id", JOB_NAME, 1, "", map);
    }

    /**
     * Get multi sharding contexts.
     * @return ShardingContexts
     */
    public static ShardingContexts getMultipleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "A");
        map.put(1, "B");
        return new ShardingContexts("fake_task_id", JOB_NAME, 2, "", map);
    }
}
