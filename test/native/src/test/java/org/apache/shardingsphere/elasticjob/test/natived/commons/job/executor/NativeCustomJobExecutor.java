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

package org.apache.shardingsphere.elasticjob.test.natived.commons.job.executor;

import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.JobRuntimeService;
import org.apache.shardingsphere.elasticjob.spi.executor.item.param.ShardingContext;
import org.apache.shardingsphere.elasticjob.spi.executor.item.type.TypedJobItemExecutor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom typed job item executor which is registered through SPI, used by the native tests.
 */
public final class NativeCustomJobExecutor implements TypedJobItemExecutor {
    
    private static final String TYPE = "NATIVE_CUSTOM";
    
    private static final Set<String> EXECUTED_JOB_NAMES = ConcurrentHashMap.newKeySet();
    
    /**
     * Judge whether a job has been executed by this executor.
     *
     * @param jobName job name
     * @return executed or not
     */
    public static boolean isExecuted(final String jobName) {
        return EXECUTED_JOB_NAMES.contains(jobName);
    }
    
    @Override
    public void process(final ElasticJob elasticJob, final JobConfiguration jobConfig, final JobRuntimeService jobRuntimeService, final ShardingContext shardingContext) {
        EXECUTED_JOB_NAMES.add(jobConfig.getJobName());
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
