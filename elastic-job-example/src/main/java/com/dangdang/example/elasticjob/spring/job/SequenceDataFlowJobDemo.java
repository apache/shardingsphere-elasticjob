/**
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

package com.dangdang.example.elasticjob.spring.job;

import com.dangdang.ddframe.job.api.JobExecutionSingleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractBatchSequenceDataFlowElasticJob;
import com.dangdang.example.elasticjob.fixture.entity.Foo;
import com.dangdang.example.elasticjob.fixture.repository.FooRepository;
import com.dangdang.example.elasticjob.utils.PrintContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class SequenceDataFlowJobDemo extends AbstractBatchSequenceDataFlowElasticJob<Foo> {
    
    private PrintContext printContext = new PrintContext(SequenceDataFlowJobDemo.class);
    
    @Resource
    private FooRepository fooRepository;
    
    @Override
    public List<Foo> fetchData(final JobExecutionSingleShardingContext context) {
        printContext.printFetchDataMessage(context.getShardingItem());
        return fooRepository.findActive(Collections.singletonList(context.getShardingItem()));
    }
    
    @Override
    public int processData(final JobExecutionSingleShardingContext context, final List<Foo> data) {
        printContext.printProcessDataMessage(data);
        int successCount = 0;
        for (Foo each : data) {
            fooRepository.setInactive(each.getId());
            successCount++;
        }
        return successCount;
    }
    
    @Override
    public boolean isStreamingProcess() {
        return true;
    }
}
