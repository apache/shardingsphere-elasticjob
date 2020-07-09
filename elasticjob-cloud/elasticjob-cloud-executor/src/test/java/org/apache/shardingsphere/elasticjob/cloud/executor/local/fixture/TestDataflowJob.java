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

package org.apache.shardingsphere.elasticjob.cloud.executor.local.fixture;

import org.apache.shardingsphere.elasticjob.cloud.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.cloud.api.dataflow.DataflowJob;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class TestDataflowJob implements DataflowJob<String> {
    
    @Setter
    private static List<String> input;
    
    @Getter
    @Setter
    private static List<String> output;
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        return input;
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        output = Lists.transform(input, new Function<String, String>() {
            @Override
            public String apply(final String input) {
                return input + "-d";
            }
        });
    }
}
