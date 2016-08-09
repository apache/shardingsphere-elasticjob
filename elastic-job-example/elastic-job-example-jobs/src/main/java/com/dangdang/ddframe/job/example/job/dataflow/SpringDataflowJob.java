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
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.example.fixture.entity.Foo;
import com.dangdang.ddframe.job.example.fixture.repository.FooRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class SpringDataflowJob implements DataflowJob<Foo> {
    
    @Resource
    private FooRepository fooRepository;
    
    @Override
    public List<Foo> fetchData(final ShardingContext shardingContext) {
        System.out.println(new Date() + ":------dataflow job fetch data-------:" + shardingContext);
        return fooRepository.findActive(Collections.singletonList(shardingContext.getShardingItemParameters().keySet().iterator().next()));
    }
    
    @Override
    public void processData(final ShardingContext shardingContext, final List<Foo> data) {
        for (Foo each : data) {
            System.out.println(new Date() + ":------dataflow job fetch data-------:" + shardingContext);
            fooRepository.setInactive(each.getId());
        }
    }
}
