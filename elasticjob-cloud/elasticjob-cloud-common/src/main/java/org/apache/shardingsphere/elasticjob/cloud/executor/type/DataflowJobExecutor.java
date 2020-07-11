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

package org.apache.shardingsphere.elasticjob.cloud.executor.type;

import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.cloud.api.dataflow.DataflowJob;
import org.apache.shardingsphere.elasticjob.cloud.config.dataflow.DataflowJobConfiguration;
import org.apache.shardingsphere.elasticjob.cloud.executor.AbstractElasticJobExecutor;
import org.apache.shardingsphere.elasticjob.cloud.executor.JobFacade;

import java.util.List;

/**
 * Dataflow job executor.
 */
public final class DataflowJobExecutor extends AbstractElasticJobExecutor {
    
    private final DataflowJob<Object> dataflowJob;
    
    public DataflowJobExecutor(final DataflowJob<Object> dataflowJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.dataflowJob = dataflowJob;
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) getJobRootConfig().getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) {
            streamingExecute(shardingContext);
        } else {
            oneOffExecute(shardingContext);
        }
    }
    
    private void streamingExecute(final ShardingContext shardingContext) {
        List<Object> data = fetchData(shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
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
