/**
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

package com.dangdang.ddframe.job.integrate.fixture.dataflow.throughput;

import java.util.Arrays;
import java.util.List;

import org.quartz.JobExecutionException;

import lombok.Getter;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.plugin.job.type.AbstractThroughputDataFlowElasticJob;

public class StreamingThroughputDataFlowElasticJobForExecuteThrowsException extends AbstractThroughputDataFlowElasticJob<String> {
    
    @Getter
    private static volatile boolean completed;
    
    @Override
    public List<String> fetchData(final JobExecutionMultipleShardingContext context) {
        if (completed) {
            return null;
        }
        return Arrays.asList("data");
    }
    
    @Override
    public boolean processData(final JobExecutionMultipleShardingContext context, final String data) {
        completed = true;
        throw new JobException("");
    }
    
    @Override
    public boolean isStreamingProcess() {
        return true;
    }
    
    @Override
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
    }
    
    public static void reset() {
        completed = false;
    }
}
