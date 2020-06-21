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

package org.apache.shardingsphere.elasticjob.lite.fixture.config;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobCoreConfiguration.Builder;
import org.apache.shardingsphere.elasticjob.lite.config.JobRootConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.simple.SimpleJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.handler.JobProperties.JobPropertiesEnum;
import org.apache.shardingsphere.elasticjob.lite.fixture.ShardingContextsBuilder;
import org.apache.shardingsphere.elasticjob.lite.fixture.handler.ThrowJobExceptionHandler;

@NoArgsConstructor
public final class TestSimpleJobConfiguration implements JobRootConfiguration {
    
    private String jobExceptionHandlerClassName;
    
    private String executorServiceHandlerClassName;
    
    public TestSimpleJobConfiguration(final String jobExceptionHandlerClassName, final String executorServiceHandlerClassName) {
        this.jobExceptionHandlerClassName = jobExceptionHandlerClassName;
        this.executorServiceHandlerClassName = executorServiceHandlerClassName;
    }
    
    @Override
    public JobTypeConfiguration getTypeConfig() {
        Builder builder = JobCoreConfiguration.newBuilder(ShardingContextsBuilder.JOB_NAME, "0/1 * * * * ?", 3)
                .shardingItemParameters("0=A,1=B,2=C").jobParameter("param").failover(true).misfire(false).description("desc");
        if (null == jobExceptionHandlerClassName) {
            builder.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), ThrowJobExceptionHandler.class.getCanonicalName());
        } else {
            builder.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandlerClassName);
        }
        if (null != executorServiceHandlerClassName) {
            builder.jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandlerClassName);
        }
        return new SimpleJobConfiguration(builder.build());
    }
}
