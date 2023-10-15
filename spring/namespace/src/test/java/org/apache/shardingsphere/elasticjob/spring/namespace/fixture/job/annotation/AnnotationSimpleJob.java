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

package org.apache.shardingsphere.elasticjob.spring.namespace.fixture.job.annotation;

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobConfiguration;
import org.apache.shardingsphere.elasticjob.annotation.ElasticJobProp;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

@Getter
@ElasticJobConfiguration(
        jobName = "simpleJob",
        registryCenter = "regCenter",
        description = "desc",
        shardingTotalCount = 3,
        shardingItemParameters = "0=a,1=b,2=c",
        cron = "*/1 * * * * ?",
        props = {
                @ElasticJobProp(key = "print.title", value = "test title"),
                @ElasticJobProp(key = "print.content", value = "test content")
        })
public final class AnnotationSimpleJob implements SimpleJob {
    
    @Getter
    private static volatile boolean completed;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        completed = true;
    }
    
    /**
     * Set completed to false.
     */
    public static void reset() {
        completed = false;
    }
}
