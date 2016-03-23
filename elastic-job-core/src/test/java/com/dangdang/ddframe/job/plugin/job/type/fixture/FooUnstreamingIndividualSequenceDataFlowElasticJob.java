/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.plugin.job.type.fixture;

import java.util.List;

import com.dangdang.ddframe.job.api.JobExecutionSingleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.dataflow.AbstractIndividualSequenceDataFlowElasticJob;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FooUnstreamingIndividualSequenceDataFlowElasticJob extends AbstractIndividualSequenceDataFlowElasticJob<Object> {
    
    private final JobCaller jobCaller;
    
    @Override
    public List<Object> fetchData(final JobExecutionSingleShardingContext shardingContext) {
        return jobCaller.fetchData(shardingContext.getShardingItem());
    }
    
    @Override
    public boolean processData(final JobExecutionSingleShardingContext shardingContext, final Object data) {
        return jobCaller.processData(data);
    }
    
    @Override
    public boolean isStreamingProcess() {
        return false;
    }
}
