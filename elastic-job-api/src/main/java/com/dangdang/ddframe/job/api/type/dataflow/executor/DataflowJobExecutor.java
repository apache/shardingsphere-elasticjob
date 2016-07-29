/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.api.type.dataflow.executor;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.internal.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 数据流作业执行器.
 * 
 * @author zhangliang
 */
@Slf4j
public final class DataflowJobExecutor extends AbstractElasticJobExecutor {
    
    private final DataflowJob<Object> dataflowJob;
    
    public DataflowJobExecutor(final DataflowJob<Object> dataflowJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.dataflowJob = dataflowJob;
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) getJobConfig().getTypeConfig();
        if (DataflowJobConfiguration.DataflowType.THROUGHPUT == dataflowConfig.getDataflowType()) {
            if (dataflowConfig.isStreamingProcess()) {
                executeThroughputStreamingJob(dataflowConfig.getConcurrentDataProcessThreadCount(), shardingContext);
            } else {
                executeThroughputOneOffJob(dataflowConfig.getConcurrentDataProcessThreadCount(), shardingContext);
            }
        } else if (DataflowJobConfiguration.DataflowType.SEQUENCE == dataflowConfig.getDataflowType()) {
            if (dataflowConfig.isStreamingProcess()) {
                executeSequenceStreamingJob(shardingContext);
            } else {
                executeSequenceOneOffJob(shardingContext);
            }
        }
    }
    
    private void executeThroughputStreamingJob(final int concurrentDataProcessThreadCount, final ShardingContext shardingContext) {
        List<Object> data = fetchDataForThroughput(shardingContext);
        while (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(concurrentDataProcessThreadCount, shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchDataForThroughput(shardingContext);
        }
    }
    
    private void executeThroughputOneOffJob(final int concurrentDataProcessThreadCount, final ShardingContext shardingContext) {
        List<Object> data = fetchDataForThroughput(shardingContext);
        if (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(concurrentDataProcessThreadCount, shardingContext, data);
        }
    }
    
    private void executeSequenceStreamingJob(final ShardingContext shardingContext) {
        Map<Integer, List<Object>> data = fetchDataForSequence(shardingContext);
        while (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchDataForSequence(shardingContext);
        }
    }
    
    private void executeSequenceOneOffJob(final ShardingContext shardingContext) {
        Map<Integer, List<Object>> data = fetchDataForSequence(shardingContext);
        if (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
        }
    }
    
    private List<Object> fetchDataForThroughput(final ShardingContext shardingContext) {
        List<Object> result = dataflowJob.fetchData(shardingContext);
        log.trace("Elastic job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    
    private void processDataForThroughput(final int concurrentDataProcessThreadCount, final ShardingContext shardingContext, final List<Object> data) {
        if (concurrentDataProcessThreadCount <= 1 || data.size() <= concurrentDataProcessThreadCount) {
            processData(shardingContext, data);
            return;
        }
        List<List<Object>> splitData = Lists.partition(data, data.size() / concurrentDataProcessThreadCount);
        final CountDownLatch latch = new CountDownLatch(splitData.size());
        for (final List<Object> each : splitData) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processData(shardingContext, each);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    private Map<Integer, List<Object>> fetchDataForSequence(final ShardingContext shardingContext) {
        Collection<Integer> items = shardingContext.getShardingItems().keySet();
        final Map<Integer, List<Object>> result = new ConcurrentHashMap<>(items.size());
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        List<Object> data = dataflowJob.fetchData(shardingContext.getShardingContext(each));
                        if (null != data && !data.isEmpty()) {
                            result.put(each, data);
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
        log.trace("Elastic job: fetch data size: {}.", result.size());
        return result;
    }
    
    private void processDataForSequence(final ShardingContext shardingContext, final Map<Integer, List<Object>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Map.Entry<Integer, List<Object>> each : data.entrySet()) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processData(shardingContext.getShardingContext(each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    private void processData(final ShardingContext shardingContext, final List<Object> data) {
        try {
            dataflowJob.processData(shardingContext, data);
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            handleException(cause);
        }
    }
    
    private void latchAwait(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
