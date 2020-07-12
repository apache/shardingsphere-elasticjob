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

package org.apache.shardingsphere.elasticjob.dataflow.executor;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.apache.shardingsphere.elasticjob.dataflow.props.DataflowJobProperties;
import org.apache.shardingsphere.elasticjob.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.executor.item.impl.ClassedJobItemExecutor;

import java.util.List;

/**
 * Dataflow job executor.
 */
public final class DataflowJobExecutor implements ClassedJobItemExecutor<DataflowJob> {
    
    @Override
    public void process(final DataflowJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        if (Boolean.parseBoolean(jobConfig.getProps().getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false).toString())) {
            streamingExecute(elasticJob, jobConfig, jobFacade, shardingContext);
        } else {
            oneOffExecute(elasticJob, shardingContext);
        }
    }
    
    private void streamingExecute(final DataflowJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        List<Object> data = fetchData(elasticJob, shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(elasticJob, shardingContext, data);
            if (!isEligibleForJobRunning(jobConfig, jobFacade)) {
                break;
            }
            data = fetchData(elasticJob, shardingContext);
        }
    }
    
    private boolean isEligibleForJobRunning(final JobConfiguration jobConfig, final JobFacade jobFacade) {
        return !jobFacade.isNeedSharding() && Boolean.parseBoolean(jobConfig.getProps().getOrDefault(DataflowJobProperties.STREAM_PROCESS_KEY, false).toString());
    }
    
    private void oneOffExecute(final DataflowJob elasticJob, final ShardingContext shardingContext) {
        List<Object> data = fetchData(elasticJob, shardingContext);
        if (null != data && !data.isEmpty()) {
            processData(elasticJob, shardingContext, data);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> fetchData(final DataflowJob elasticJob, final ShardingContext shardingContext) {
        return elasticJob.fetchData(shardingContext);
    }
    
    @SuppressWarnings("unchecked")
    private void processData(final DataflowJob elasticJob, final ShardingContext shardingContext, final List<Object> data) {
        elasticJob.processData(shardingContext, data);
    }
    
    @Override
    public Class<DataflowJob> getElasticJobClass() {
        return DataflowJob.class;
    }
}
