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

package org.apache.shardingsphere.elasticjob.lite.executor.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.lite.config.JobRootConfiguration;
import org.apache.shardingsphere.elasticjob.lite.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.executor.JobFacade;

import java.util.List;

/**
 * Dataflow job executor.
 */
@RequiredArgsConstructor
public final class DataflowJobExecutor implements JobItemExecutor {
    
    private final DataflowJob<Object> dataflowJob;
    
    @Override
    public void process(final JobRootConfiguration jobRootConfig, final JobFacade jobFacade, final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) jobRootConfig.getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) {
            streamingExecute(jobFacade, shardingContext);
        } else {
            oneOffExecute(shardingContext);
        }
    }
    
    private void streamingExecute(final JobFacade jobFacade, final ShardingContext shardingContext) {
        List<Object> data = fetchData(shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(shardingContext, data);
            if (!jobFacade.isEligibleForJobRunning()) {
                break;
            }
            data = fetchData(shardingContext);
        }
    }
    
    private void oneOffExecute(final ShardingContext shardingContext) {
        List<Object> data = fetchData(shardingContext);
        if (null != data && !data.isEmpty()) {
            processData(shardingContext, data);
        }
    }
    
    private List<Object> fetchData(final ShardingContext shardingContext) {
        return dataflowJob.fetchData(shardingContext);
    }
    
    private void processData(final ShardingContext shardingContext, final List<Object> data) {
        dataflowJob.processData(shardingContext, data);
    }
}
