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

package com.dangdang.ddframe.job.example.job.dataflow;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class JavaDataflowJob implements DataflowJob<String> {
    
    private int count;
    
    @Override
    public List<String> fetchData(final ShardingContext shardingContext) {
        System.out.println(String.format("------Thread ID: %s, Date: %s, Sharding Context: %s, Action: %s", Thread.currentThread().getId(), new Date(), shardingContext, "dataflow job fetch data"));
        count++;
        if (count > 10) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Integer.toString(new Random().nextInt(10)));
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<String> data) {
        System.out.println(String.format("------Thread ID: %s, Date: %s, Sharding Context: %s, Action: %s, Data: %s", 
                Thread.currentThread().getId(), new Date(), shardingContext, "dataflow job process data", data));
    }
}
