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

package com.dangdang.ddframe.job.fixture.config;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties;
import com.dangdang.ddframe.job.fixture.ShardingContextsBuilder;
import com.dangdang.ddframe.job.fixture.handler.ThrowJobExceptionHandler;
import com.dangdang.ddframe.job.fixture.job.TestSimpleJob;
import lombok.NoArgsConstructor;

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
        JobCoreConfiguration.Builder builder = JobCoreConfiguration.newBuilder(ShardingContextsBuilder.JOB_NAME, "0/1 * * * * ?", 3)
                .shardingItemParameters("0=A,1=B,2=C").jobParameter("param").failover(true).misfire(false).description("desc");
        if (null == jobExceptionHandlerClassName) {
            builder.jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), ThrowJobExceptionHandler.class.getCanonicalName());
        } else {
            builder.jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandlerClassName);
        }
        if (null != executorServiceHandlerClassName) {
            builder.jobProperties(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), executorServiceHandlerClassName);
        }
        return new SimpleJobConfiguration(builder.build(), TestSimpleJob.class.getCanonicalName());
    }
}
