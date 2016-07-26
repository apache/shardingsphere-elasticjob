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

package com.dangdang.ddframe.job.cloud.example;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.DataflowElasticJob;

import java.util.Collections;
import java.util.List;

public class DataflowCloudJob implements DataflowElasticJob<String> {
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        System.out.println("---fetch data:" + shardingContext);
        return Collections.singletonList("abc");
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        System.out.println("---process data:" + data);
    }
}
