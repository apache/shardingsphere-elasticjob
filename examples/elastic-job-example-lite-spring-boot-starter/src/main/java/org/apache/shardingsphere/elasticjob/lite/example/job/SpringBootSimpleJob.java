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

package org.apache.shardingsphere.elasticjob.lite.example.job;

import org.apache.shardingsphere.elasticjob.lite.api.job.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.example.entity.Foo;
import org.apache.shardingsphere.elasticjob.lite.example.repository.FooRepository;
import org.apache.shardingsphere.elasticjob.lite.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.ElasticJob;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.JobBootstrapType;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.RegistryCenter;
import org.apache.shardingsphere.elasticjob.lite.springboot.annotation.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@ElasticJob(
        jobBootstrapType = JobBootstrapType.SCHEDULE,
        jobName = "beanSimpleJob",
        shardingTotalCount = 3,
        shardingItemParameters = "0=Beijing,1=Shanghai,2=Guangzhou",
        cron = "0/10 * * * * ?"
)
@Tracing
@RegistryCenter
public class SpringBootSimpleJob implements SimpleJob {

    @Autowired
    private FooRepository fooRepository;

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println(String.format("Item: %s | Time: %s | Thread: %s | %s",
                shardingContext.getShardingItem(), new SimpleDateFormat("HH:mm:ss").format(new Date()), Thread.currentThread().getId(), "SIMPLE"));
        List<Foo> data = fooRepository.findTodoData(shardingContext.getShardingParameter(), 10);
        for (Foo each : data) {
            fooRepository.setCompleted(each.getId());
        }
    }
}
