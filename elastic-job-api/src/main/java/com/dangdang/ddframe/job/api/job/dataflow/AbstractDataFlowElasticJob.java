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

package com.dangdang.ddframe.job.api.job.dataflow;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.job.AbstractElasticJob;
import com.dangdang.ddframe.job.exception.JobException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于处理数据流程的作业抽象类.
 * 
 * @author zhangliang
 * 
 * @param <T> 数据流作业处理的数据实体类型
 */
@Slf4j
public abstract class AbstractDataflowElasticJob<T> extends AbstractElasticJob implements DataflowElasticJob<T> {
    
    private final ExecutorService executorService;
    
    public AbstractDataflowElasticJob() {
        executorService = getExecutorService();
    }
    
    @Override
    protected final void executeJob(final ShardingContext shardingContext) {
        DataflowType dataflowType = getJobFacade().getDataflowType();
        boolean streamingProcess = getJobFacade().isStreamingProcess();
        if (DataflowType.THROUGHPUT == dataflowType) {
            if (streamingProcess) {
                executeThroughputStreamingJob(shardingContext);
            } else {
                executeThroughputOneOffJob(shardingContext);
            }
        } else if (DataflowType.SEQUENCE == dataflowType) {
            if (streamingProcess) {
                executeSequenceStreamingJob(shardingContext);
            } else {
                executeSequenceOneOffJob(shardingContext);
            }
        }
    }
    
    private void executeThroughputStreamingJob(final ShardingContext shardingContext) {
        List<T> data = fetchDataForThroughput(shardingContext);
        while (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchDataForThroughput(shardingContext);
        }
    }
    
    private void executeThroughputOneOffJob(final ShardingContext shardingContext) {
        List<T> data = fetchDataForThroughput(shardingContext);
        if (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(shardingContext, data);
        }
    }
    
    private void executeSequenceStreamingJob(final ShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        while (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchDataForSequence(shardingContext);
        }
    }
    
    private void executeSequenceOneOffJob(final ShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        if (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
        }
    }
    
    private List<T> fetchDataForThroughput(final ShardingContext shardingContext) {
        @SuppressWarnings("unchecked")
        List<T> result = fetchData(shardingContext);
        log.trace("Elastic job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void processDataForThroughput(final ShardingContext shardingContext, final List<T> data) {
        int threadCount = getJobFacade().getConcurrentDataProcessThreadCount();
        if (threadCount <= 1 || data.size() <= threadCount) {
            processDataWithStatistics(shardingContext, data);
            return;
        }
        List<List<T>> splitData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitData.size());
        for (final List<T> each : splitData) {
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
    
    private Map<Integer, List<T>> fetchDataForSequence(final ShardingContext shardingContext) {
        Collection<Integer> items = shardingContext.getShardingItems().keySet();
        final Map<Integer, List<T>> result = new ConcurrentHashMap<>(items.size());
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        List<T> data = fetchData(shardingContext.getShardingContext(each));
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
    
    @SuppressWarnings("unchecked")
    private void processDataForSequence(final ShardingContext shardingContext, final Map<Integer, List<T>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Entry<Integer, List<T>> each : data.entrySet()) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataWithStatistics(shardingContext.getShardingContext(each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    private void processDataWithStatistics(final ShardingContext shardingContext, final List<T> data) {
        try {
            processData(shardingContext, data);
            ProcessCountStatistics.incrementProcessSuccessCount(shardingContext.getJobName());
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            ProcessCountStatistics.incrementProcessFailureCount(shardingContext.getJobName());
            handleJobExecutionException(new JobException(cause));
        }
    }
    
    @Override
    public final void updateOffset(final int item, final String offset) {
        getJobFacade().updateOffset(item, offset);
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }
    
    @Override
    public void handleJobExecutionException(final JobException jobException) {
        log.error("Elastic job: exception occur in job processing...", jobException.getCause());
    }
    
    private void latchAwait(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
