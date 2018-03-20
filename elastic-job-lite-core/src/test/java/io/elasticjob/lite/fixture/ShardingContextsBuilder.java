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

package io.elasticjob.lite.fixture;

import io.elasticjob.lite.executor.ShardingContexts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingContextsBuilder {
    
    public static final String JOB_NAME = "test_job";
    
    public static ShardingContexts getSingleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(1, 1);
        map.put(0, "A");
        return new ShardingContexts("fake_task_id", JOB_NAME, 1, "", map);
    }
    
    public static ShardingContexts getMultipleShardingContexts() {
        Map<Integer, String> map = new HashMap<>(2, 1);
        map.put(0, "A");
        map.put(1, "B");
        return new ShardingContexts("fake_task_id", JOB_NAME, 2, "", map);
    }
}
