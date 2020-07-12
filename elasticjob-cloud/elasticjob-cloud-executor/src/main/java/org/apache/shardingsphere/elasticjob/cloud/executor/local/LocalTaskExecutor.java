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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.ElasticJob;
import org.apache.shardingsphere.elasticjob.api.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.cloud.api.JobType;
import org.apache.shardingsphere.elasticjob.cloud.config.JobCoreConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.JobTypeConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.config.script.ScriptJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.CloudJobFacade;
import org.apache.shardingsphere.elasticjob.cloud.executor.JobTypeConfigurationUtil;
import org.apache.shardingsphere.elasticjob.cloud.util.config.ShardingItemParameters;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.executor.ElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobSystemException;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.elasticjob.tracing.JobEventBus;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Local task executor.
 */
@RequiredArgsConstructor
public final class LocalTaskExecutor {
    
    private final LocalCloudJobConfiguration localJobConfiguration;
    
    /**
     * Execute job.
     */
    @SuppressWarnings("unchecked")
    public void execute() {
        CloudJobFacade jobFacade = new CloudJobFacade(getShardingContexts(), getJobTypeConfiguration(), new JobEventBus());
        ElasticJob elasticJob;
        switch (localJobConfiguration.getTypeConfig().getJobType()) {
            case SIMPLE:
                elasticJob = getJobInstance(SimpleJob.class);
                break;
            case DATAFLOW:
                elasticJob = getJobInstance(DataflowJob.class);
                break;
            default:
                elasticJob = null;
        }
        if (null == elasticJob) {
            new ElasticJobExecutor("SCRIPT", jobFacade.loadJobConfiguration(true), jobFacade).execute();
        } else {
            new ElasticJobExecutor(elasticJob, jobFacade.loadJobConfiguration(true), jobFacade).execute();
        }
    }
    
    private ShardingContexts getShardingContexts() {
        JobCoreConfiguration coreConfig = localJobConfiguration.getTypeConfig().getCoreConfig();
        Map<Integer, String> shardingItemMap = new HashMap<>(1, 1);
        shardingItemMap.put(localJobConfiguration.getShardingItem(),
                new ShardingItemParameters(coreConfig.getShardingItemParameters()).getMap().get(localJobConfiguration.getShardingItem()));
        return new ShardingContexts(Joiner.on("@-@").join(localJobConfiguration.getJobName(), localJobConfiguration.getShardingItem(), "READY", "foo_slave_id", "foo_uuid"),
                localJobConfiguration.getJobName(), coreConfig.getShardingTotalCount(), coreConfig.getJobParameter(), shardingItemMap);
    }
    
    private JobTypeConfiguration getJobTypeConfiguration() {
        Map<String, String> jobConfigurationMap = new HashMap<>();
        jobConfigurationMap.put("jobClass", localJobConfiguration.getTypeConfig().getJobClass());
        jobConfigurationMap.put("jobType", localJobConfiguration.getTypeConfig().getJobType().name());
        jobConfigurationMap.put("jobName", localJobConfiguration.getJobName());
        if (JobType.DATAFLOW == localJobConfiguration.getTypeConfig().getJobType()) {
            jobConfigurationMap.put("streamingProcess", Boolean.toString(((DataflowJobConfiguration) localJobConfiguration.getTypeConfig()).isStreamingProcess()));
        } else if (JobType.SCRIPT == localJobConfiguration.getTypeConfig().getJobType()) {
            jobConfigurationMap.put("scriptCommandLine", ((ScriptJobConfiguration) localJobConfiguration.getTypeConfig()).getScriptCommandLine());
        }
        return JobTypeConfigurationUtil.createJobConfigurationContext(jobConfigurationMap);
    }
    
    private <T extends ElasticJob> T getJobInstance(final Class<T> clazz) {
        Object result;
        if (Strings.isNullOrEmpty(localJobConfiguration.getApplicationContext())) {
            String jobClass = localJobConfiguration.getTypeConfig().getJobClass();
            try {
                result = Class.forName(jobClass).newInstance();
            } catch (final ReflectiveOperationException ex) {
                throw new JobSystemException("ElasticJob: Class '%s' initialize failure, the error message is '%s'.", jobClass, ex.getMessage());
            }
        } else {
            result = new ClassPathXmlApplicationContext(localJobConfiguration.getApplicationContext()).getBean(localJobConfiguration.getBeanName());
        }
        return clazz.cast(result);
    }
}
