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

package org.apache.shardingsphere.elasticjob.cloud.fixture.config;

import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.JobExceptionHandler;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.JobRootConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.handler.JobProperties;
import org.apache.shardingsphere.elasticjob.cloud.fixture.ShardingContextsBuilder;
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
