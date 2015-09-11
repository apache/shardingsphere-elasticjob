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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dangdang.ddframe.job.api.AbstractOneOffElasticJob;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.example.elasticjob.fixture.repository.FooRepository;

@Component
public class OneOffElasticDemoJob extends AbstractOneOffElasticJob {
    
    private static final String MESSAGE = "------ OneOffElasticDemoJob process: %s at %s ------";
    
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    @Resource
    private FooRepository fooRepository;
    
    @Override
    protected void process(final JobExecutionMultipleShardingContext context) {
        System.out.println(String.format(MESSAGE, context.getShardingItems(), new SimpleDateFormat(DATE_FORMAT).format(new Date())));
        fooRepository.findActive(context.getShardingItems());
        // do something
    }
}
