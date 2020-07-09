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

package org.apache.shardingsphere.elasticjob.lite.example.config;

import org.apache.shardingsphere.elasticjob.lite.example.job.BeanSimpleJob;
import org.apache.shardingsphere.elasticjob.lite.example.repository.FooRepository;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.JobBootstrapType;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.Tracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuweijie
 */
@Configuration
public class BeanJobConfiguration {

    @Bean
    @ElasticJob(
            jobName = "springBootBeanSimpleJob",
            shardingTotalCount = 3,
            shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou",
            cron = "0/10 * * * * ?",
            jobBootstrapType = JobBootstrapType.SCHEDULE)
    @Tracing
    public BeanSimpleJob springBootBeanSimpleJob(FooRepository fooRepository) {
        return new BeanSimpleJob(fooRepository);
    }
}
