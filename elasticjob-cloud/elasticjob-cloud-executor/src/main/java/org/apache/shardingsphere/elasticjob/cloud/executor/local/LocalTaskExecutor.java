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

package org.apache.shardingsphere.elasticjob.cloud.executor.local;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.facade.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.infra.context.ShardingItemParameters;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.tracing.JobTracingEventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Local task executor.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalTaskExecutor {
    
    private final ElasticJob elasticJob;
    
    private final String elasticJobType;
    
    private final JobConfiguration jobConfiguration;
    
    private final int shardingItem;
    
    public LocalTaskExecutor(final ElasticJob elasticJob, final JobConfiguration jobConfiguration, final int shardingItem) {
        this(elasticJob, null, jobConfiguration, shardingItem);
    }
    
    public LocalTaskExecutor(final String elasticJobType, final JobConfiguration jobConfiguration, final int shardingItem) {
        this(null, elasticJobType, jobConfiguration, shardingItem);
    }
    
    /**
     * Execute job.
     */
    public void execute() {
        createElasticJobExecutor(new CloudJobFacade(getShardingContexts(), jobConfiguration, new JobTracingEventBus())).execute();
    }
    
    private ElasticJobExecutor createElasticJobExecutor(final JobFacade jobFacade) {
        return null == elasticJob ? new ElasticJobExecutor(elasticJobType, jobConfiguration, jobFacade) : new ElasticJobExecutor(elasticJob, jobConfiguration, jobFacade);
    }
    
    private ShardingContexts getShardingContexts() {
        Map<Integer, String> shardingItemMap = new HashMap<>(1, 1);
        shardingItemMap.put(shardingItem, new ShardingItemParameters(jobConfiguration.getShardingItemParameters()).getMap().get(shardingItem));
        String taskId = String.join("@-@", jobConfiguration.getJobName(), shardingItem + "", "READY", "foo_slave_id", "foo_uuid");
        return new ShardingContexts(taskId, jobConfiguration.getJobName(), jobConfiguration.getShardingTotalCount(), jobConfiguration.getJobParameter(), shardingItemMap);
    }
}
