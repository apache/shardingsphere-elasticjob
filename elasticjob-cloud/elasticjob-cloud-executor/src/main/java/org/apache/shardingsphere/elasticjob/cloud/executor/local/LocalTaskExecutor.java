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
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.cloud.executor.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.cloud.executor.JobTypeConfigurationUtil;
import org.apache.shardingsphere.elasticjob.cloud.util.config.ShardingItemParameters;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
        createElasticJobExecutor(new CloudJobFacade(getShardingContexts(), getJobConfiguration(), new JobEventBus())).execute();
    }
    
    private ElasticJobExecutor createElasticJobExecutor(final JobFacade jobFacade) {
        return null == elasticJob
                ? new ElasticJobExecutor(elasticJobType, jobConfiguration, jobFacade)
                : new ElasticJobExecutor(elasticJob, jobConfiguration, jobFacade);
    }
    
    private ShardingContexts getShardingContexts() {
        Map<Integer, String> shardingItemMap = new HashMap<>(1, 1);
        shardingItemMap.put(shardingItem, new ShardingItemParameters(jobConfiguration.getShardingItemParameters()).getMap().get(shardingItem));
        String taskId = new StringJoiner("@-@").add(jobConfiguration.getJobName()).add(shardingItem + "").add("READY").add("foo_slave_id").add("foo_uuid").toString();
        return new ShardingContexts(taskId, jobConfiguration.getJobName(), jobConfiguration.getShardingTotalCount(), jobConfiguration.getJobParameter(), shardingItemMap);
    }
    
    private JobConfiguration getJobConfiguration() {
        Map<String, String> jobConfigurationMap = new HashMap<>();
        jobConfigurationMap.put("jobName", jobConfiguration.getJobName());
        if (jobConfiguration.getProps().containsKey("streaming.process")) {
            jobConfigurationMap.put("streamingProcess", jobConfiguration.getProps().getProperty("streaming.process"));
        }
        if (jobConfiguration.getProps().containsKey("script.command.line")) {
            jobConfigurationMap.put("scriptCommandLine", jobConfiguration.getProps().getProperty("script.command.line"));
        }
        return JobTypeConfigurationUtil.createJobConfigurationContext(jobConfigurationMap);
    }
}
