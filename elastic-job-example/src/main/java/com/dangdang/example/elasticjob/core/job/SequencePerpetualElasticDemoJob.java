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

package com.dangdang.example.elasticjob.core.job;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.dangdang.ddframe.job.api.AbstractSequencePerpetualElasticJob;
import com.dangdang.ddframe.job.api.JobExecutionSingleShardingContext;
import com.dangdang.example.elasticjob.fixture.entity.Foo;
import com.dangdang.example.elasticjob.fixture.repository.FooRepository;

public class SequencePerpetualElasticDemoJob extends AbstractSequencePerpetualElasticJob<Foo> {
    
    private static final String LOAD_MESSAGE = "------ SequencePerpetualElasticDemoJob load sharding items: %s at %s ------";
    
    private static final String PROCESS_MESSAGE = "------ SequencePerpetualElasticDemoJob process data: %s at %s ------";
    
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private FooRepository fooRepository = new FooRepository();
    
    @Override
    protected List<Foo> fetchData(final JobExecutionSingleShardingContext context) {
        System.out.println(String.format(LOAD_MESSAGE, context.getShardingItem(), new SimpleDateFormat(DATE_FORMAT).format(new Date())));
        return fooRepository.findActive(Arrays.asList(context.getShardingItem()));
    }
    
    @Override
    protected boolean processData(final JobExecutionSingleShardingContext context, final Foo data) {
        System.out.println(String.format(PROCESS_MESSAGE, data, new SimpleDateFormat(DATE_FORMAT).format(new Date())));
        fooRepository.setInactive(data.getId());
        return true;
    }
}
