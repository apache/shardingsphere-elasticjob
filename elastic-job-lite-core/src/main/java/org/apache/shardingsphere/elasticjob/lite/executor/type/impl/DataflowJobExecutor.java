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

package org.apache.shardingsphere.elasticjob.lite.executor.type.impl;

import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;
import org.apache.shardingsphere.elasticjob.lite.executor.type.JobItemExecutor;

import java.util.List;

/**
 * Dataflow job executor.
 */
public final class DataflowJobExecutor implements JobItemExecutor<DataflowJob> {
    
    @Override
    public void process(final DataflowJob elasticJob, final JobConfiguration jobConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) jobConfig.getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) {
            streamingExecute(elasticJob, jobFacade, shardingContext);
        } else {
            oneOffExecute(elasticJob, shardingContext);
        }
    }
    
    private void streamingExecute(final DataflowJob elasticJob, final JobFacade jobFacade, final ShardingContext shardingContext) {
        List<Object> data = fetchData(elasticJob, shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(elasticJob, shardingContext, data);
            if (!jobFacade.isEligibleForJobRunning()) {
                break;
            }
            data = fetchData(elasticJob, shardingContext);
        }
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
}
