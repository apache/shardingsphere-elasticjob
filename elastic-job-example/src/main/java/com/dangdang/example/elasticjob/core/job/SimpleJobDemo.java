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

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.dangdang.example.elasticjob.fixture.repository.FooRepository;
import com.dangdang.example.elasticjob.utils.PrintContext;

public class SimpleJobDemo extends AbstractSimpleElasticJob {
    
    private PrintContext printContext = new PrintContext(SimpleJobDemo.class);
    
    private FooRepository fooRepository = new FooRepository();
    
    @Override
    public void process(final JobExecutionMultipleShardingContext context) {
        printContext.printProcessJobMessage(context.getShardingItems());
        fooRepository.findActive(context.getShardingItems());
        // do something
    }
}
