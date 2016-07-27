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

import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.internal.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简单作业执行器.
 * 
 * @author zhangliang
 */
@Slf4j
public final class DataflowJobExecutor extends AbstractElasticJobExecutor {
    
    private final DataflowJob<Object> dataflowElasticJob;
    
    private ExecutorService executorService;
    
    public DataflowJobExecutor(final DataflowJob dataflowElasticJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.dataflowElasticJob = dataflowElasticJob;
        executorService = Executors.newCachedThreadPool();
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        DataflowJobConfiguration.DataflowType dataflowType = getJobFacade().getDataflowType();
        boolean streamingProcess = getJobFacade().isStreamingProcess();
        if (DataflowJobConfiguration.DataflowType.THROUGHPUT == dataflowType) {
            if (streamingProcess) {
                executeThroughputStreamingJob(shardingContext);
            } else {
                executeThroughputOneOffJob(shardingContext);
            }
        } else if (DataflowJobConfiguration.DataflowType.SEQUENCE == dataflowType) {
            if (streamingProcess) {
                executeSequenceStreamingJob(shardingContext);
            } else {
                executeSequenceOneOffJob(shardingContext);
            }
        }
    }
    
    private void executeThroughputStreamingJob(final ShardingContext shardingContext) {
        List<Object> data = fetchDataForThroughput(shardingContext);
        while (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchDataForThroughput(shardingContext);
        }
    }
    
    private void executeThroughputOneOffJob(final ShardingContext shardingContext) {
        List<Object> data = fetchDataForThroughput(shardingContext);
        if (!CollectionUtils.isEmpty(data)) {
            processDataForThroughput(shardingContext, data);
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
        List<Object> result = dataflowElasticJob.fetchData(shardingContext);
        log.trace("Elastic job: fetch data size: {}.", result != null ? result.size() : 0);
        return result;
    }
    
    private void processDataForThroughput(final ShardingContext shardingContext, final List<Object> data) {
        int threadCount = getJobFacade().getConcurrentDataProcessThreadCount();
        if (threadCount <= 1 || data.size() <= threadCount) {
            processData(shardingContext, data);
            return;
        }
        List<List<Object>> splitData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitData.size());
        for (final List<Object> each : splitData) {
            executorService.submit(new Runnable() {
                
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
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        List<Object> data = dataflowElasticJob.fetchData(shardingContext.getShardingContext(each));
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
            executorService.submit(new Runnable() {
                
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
            dataflowElasticJob.processData(shardingContext, data);
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
    
    /**
     * 设置线程执行服务.
     * 
     * @param executorService 线程执行服务
     */
    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }
}
