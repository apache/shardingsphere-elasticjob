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

package com.dangdang.ddframe.job.plugin.job.type;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.job.AbstractDataFlowElasticJob;
import com.google.common.collect.Lists;

/**
 * 高吞吐量处理数据流程的作业.
 * 
 * @author zhangliang
 * 
 * @param <T> 数据流作业处理的数据实体类型
 */
@Slf4j
public abstract class AbstractThroughputDataFlowElasticJob<T> extends AbstractDataFlowElasticJob<T, JobExecutionMultipleShardingContext> {
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Override
    protected final void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        if (isStreamingProcess()) {
            executeStreamingJob(shardingContext);
        } else {
            executeOneOffJob(shardingContext);
        }
    }
    
    @Override
    protected  void afterAllShardingFinishedInternal(){
    	if (!isStreamingProcess()) {
    		afterAllUnStreamingShardingFinished();
        }
    }

    @Override
	public void afterAllUnStreamingShardingFinished() {
	}
    
    private void executeStreamingJob(final JobExecutionMultipleShardingContext shardingContext) {
        List<T> data = fetchDataWithLog(shardingContext);
        while (null != data && !data.isEmpty() && !isStoped() && !getShardingService().isNeedSharding()) {
            concurrentProcessData(shardingContext, data);
            data = fetchDataWithLog(shardingContext);
        }
    }
    
    private void executeOneOffJob(final JobExecutionMultipleShardingContext shardingContext) {
        List<T> data = fetchDataWithLog(shardingContext);
        if (null != data && !data.isEmpty()) {
            concurrentProcessData(shardingContext, data);
        }
    }
    
    private List<T> fetchDataWithLog(final JobExecutionMultipleShardingContext shardingContext) {
        List<T> result = fetchData(shardingContext);
        log.debug("Elastic job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    
    private void concurrentProcessData(final JobExecutionMultipleShardingContext shardingContext, final List<T> data) {
        int threadCount = getConfigService().getConcurrentDataProcessThreadCount();
        if (threadCount <= 1 || data.size() <= threadCount) {
            processDataWithStatistics(shardingContext, data);
            return;
        }
        List<List<T>> splitedData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitedData.size());
        for (final List<T> each : splitedData) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataWithStatistics(shardingContext, each);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
}
