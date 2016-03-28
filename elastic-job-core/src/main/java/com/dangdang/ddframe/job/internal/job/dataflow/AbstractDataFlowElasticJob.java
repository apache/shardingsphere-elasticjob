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

package com.dangdang.ddframe.job.internal.job.dataflow;

import com.dangdang.ddframe.job.api.DataFlowElasticJob;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.api.JobExecutionSingleShardingContext;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;
import com.dangdang.ddframe.job.internal.job.AbstractJobExecutionShardingContext;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
 * @param <C> 作业运行时分片上下文类型
 */
@Slf4j
public abstract class AbstractDataFlowElasticJob<T, C extends AbstractJobExecutionShardingContext> extends AbstractElasticJob implements DataFlowElasticJob<T, C> {
    
    private final ExecutorService executorService;
    
    private final DataFlowType dataFlowType;
    
    public AbstractDataFlowElasticJob() {
        executorService = getExecutorService();
        dataFlowType = getDataFlowType();
    }
    
    private DataFlowType getDataFlowType() {
        Class<?> target = getClass();
        while (true) {
            if (!(target.getGenericSuperclass() instanceof ParameterizedType)) {
                target = target.getSuperclass();
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType) target.getGenericSuperclass();
            if (2 != parameterizedType.getActualTypeArguments().length) {
                target = target.getSuperclass();
                continue;
            }
            Type type = parameterizedType.getActualTypeArguments()[1];
            if (JobExecutionMultipleShardingContext.class == type) {
                return DataFlowType.THROUGHPUT;
            } else if (JobExecutionSingleShardingContext.class == type) {
                return DataFlowType.SEQUENCE;
            } else {
                throw new UnsupportedOperationException(String.format("Cannot support %s", type));
            }
        }
    }
    
    @Override
    protected final void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        if (DataFlowType.THROUGHPUT == dataFlowType) {
            if (isStreamingProcess()) {
                executeThroughputStreamingJob(shardingContext);
            } else {
                executeThroughputOneOffJob(shardingContext);
            }
        } else if (DataFlowType.SEQUENCE == dataFlowType) {
            if (isStreamingProcess()) {
                executeSequenceStreamingJob(shardingContext);
            } else {
                executeSequenceOneOffJob(shardingContext);
            }
        }
    }
    
    private void executeThroughputStreamingJob(final JobExecutionMultipleShardingContext shardingContext) {
        // TODO isEligibleForJobRunning应该在fetchData之前也做判断, fetchData应与processData成对执行
        List<T> data = fetchDataForThroughput(shardingContext);
        while (null != data && !data.isEmpty() && getJobFacade().isEligibleForJobRunning()) {
            processDataForThroughput(shardingContext, data);
            data = fetchDataForThroughput(shardingContext);
        }
    }
    
    private void executeThroughputOneOffJob(final JobExecutionMultipleShardingContext shardingContext) {
        List<T> data = fetchDataForThroughput(shardingContext);
        if (null != data && !data.isEmpty()) {
            processDataForThroughput(shardingContext, data);
        }
    }
    
    private void executeSequenceStreamingJob(final JobExecutionMultipleShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        while (!data.isEmpty() && getJobFacade().isEligibleForJobRunning()) {
            processDataForSequence(shardingContext, data);
            data = fetchDataForSequence(shardingContext);
        }
    }
    
    private void executeSequenceOneOffJob(final JobExecutionMultipleShardingContext shardingContext) {
        Map<Integer, List<T>> data = fetchDataForSequence(shardingContext);
        if (!data.isEmpty()) {
            processDataForSequence(shardingContext, data);
        }
    }
    
    private List<T> fetchDataForThroughput(final JobExecutionMultipleShardingContext shardingContext) {
        @SuppressWarnings("unchecked")
        List<T> result = fetchData((C) shardingContext);
        log.trace("Elastic job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void processDataForThroughput(final JobExecutionMultipleShardingContext shardingContext, final List<T> data) {
        int threadCount = getJobFacade().getConcurrentDataProcessThreadCount();
        if (threadCount <= 1 || data.size() <= threadCount) {
            processDataWithStatistics((C) shardingContext, data);
            return;
        }
        List<List<T>> splitData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitData.size());
        for (final List<T> each : splitData) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataWithStatistics((C) shardingContext, each);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    private Map<Integer, List<T>> fetchDataForSequence(final JobExecutionMultipleShardingContext shardingContext) {
        List<Integer> items = shardingContext.getShardingItems();
        final Map<Integer, List<T>> result = new ConcurrentHashMap<>(items.size());
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        @SuppressWarnings("unchecked")
                        List<T> data = fetchData((C) shardingContext.createJobExecutionSingleShardingContext(each));
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
    private void processDataForSequence(final JobExecutionMultipleShardingContext shardingContext, final Map<Integer, List<T>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Entry<Integer, List<T>> each : data.entrySet()) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataWithStatistics((C) shardingContext.createJobExecutionSingleShardingContext(each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    protected abstract void processDataWithStatistics(C shardingContext, List<T> data);
    
    @Override
    public final void updateOffset(final int item, final String offset) {
        getJobFacade().updateOffset(item, offset);
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }
    
    @Override
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
        log.error("Elastic job: exception occur in job processing...", jobExecutionException.getCause());
    }
    
    private void latchAwait(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
