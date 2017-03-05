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

package com.dangdang.ddframe.job.lite.spring.fixture.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class DataflowElasticJob implements DataflowJob<String> {
    
    @Getter
    private static volatile boolean completed;
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        if (completed) {
            return Collections.emptyList();
        }
        return Collections.singletonList("data");
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        completed = true;
    }
    
    public static void reset() {
        completed = false;
    }
}
