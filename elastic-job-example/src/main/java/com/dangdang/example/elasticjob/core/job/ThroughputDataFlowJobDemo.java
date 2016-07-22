/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.example.elasticjob.core.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.job.dataflow.AbstractDataflowElasticJob;
import com.dangdang.example.elasticjob.fixture.entity.Foo;
import com.dangdang.example.elasticjob.fixture.repository.FooRepository;
import com.dangdang.example.elasticjob.utils.PrintContext;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThroughputDataflowJobDemo extends AbstractDataflowElasticJob<Foo> {
    
    private PrintContext printContext = new PrintContext(ThroughputDataflowJobDemo.class);
    
    private FooRepository fooRepository = new FooRepository();
    
    @Override
    public List<Foo> fetchData(final ShardingContext context) {
        printContext.printFetchDataMessage(context.getShardingItems().keySet());
        return fooRepository.findActive(context.getShardingItems().keySet());
    }
    
    @Override
    public void processData(final ShardingContext context, final List<Foo> data) {
        printContext.printProcessDataMessage(data);
        for (Foo each : data) {
            fooRepository.setInactive(each.getId());
        }
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(10);
    }
}
