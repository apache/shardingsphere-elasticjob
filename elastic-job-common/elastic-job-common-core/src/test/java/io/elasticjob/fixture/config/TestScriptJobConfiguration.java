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

package io.elasticjob.fixture.config;

import io.elasticjob.config.JobCoreConfiguration;
import io.elasticjob.config.JobRootConfiguration;
import io.elasticjob.config.JobTypeConfiguration;
import io.elasticjob.config.script.ScriptJobConfiguration;
import io.elasticjob.executor.handler.JobExceptionHandler;
import io.elasticjob.executor.handler.JobProperties;
import io.elasticjob.fixture.ShardingContextsBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TestScriptJobConfiguration implements JobRootConfiguration {
    
    private final String scriptCommandLine;
    
    private final Class<? extends JobExceptionHandler> jobExceptionHandlerClass;
    
    @Override
    public JobTypeConfiguration getTypeConfig() {
        return new ScriptJobConfiguration(JobCoreConfiguration.newBuilder(ShardingContextsBuilder.JOB_NAME, "0/1 * * * * ?", 3)
                .jobProperties(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobExceptionHandlerClass.getCanonicalName()).build(), scriptCommandLine);
    }
}
