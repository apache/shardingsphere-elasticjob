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

package com.dangdang.ddframe.job.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;

/**
 * 不断获取并处理最新数据并且按照分片个数启动线程执行分布式作业的基类.
 * 
 * <p>
 * 同一个分片用同一个线程处理, 保证顺序性和一致性, 类似kafka.
 * </p>
 * 
 * @author zhangliang
 * 
 * @param <T> 执行作业的实体类型
 */
@Slf4j
public abstract class AbstractSequencePerpetualElasticJob<T> extends AbstractElasticJob {
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Override
    protected final void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        Map<Integer, List<T>> data = takeDataInMultipleThreads(shardingContext);
        log.debug("Elastic job: sequence perpetual elastic job fetch data size: {}.", data != null ? data.size() : 0);
        while (!data.isEmpty() && !isStoped() && !getShardingService().isNeedSharding()) {
            processDataInMultipleThreads(shardingContext, data);
            data = takeDataInMultipleThreads(shardingContext);
            log.debug("Elastic job: sequence perpetual elastic job fetch data size: {}.", data != null ? data.size() : 0);
        }
    }
    
    private Map<Integer, List<T>> takeDataInMultipleThreads(final JobExecutionMultipleShardingContext shardingContext) {
        List<Integer> items = shardingContext.getShardingItems();
        final Map<Integer, List<T>> result = new ConcurrentHashMap<>(items.size());
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        List<T> data = fetchData(shardingContext.createJobExecutionSingleShardingContext(each));
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
        return result;
    }
    
    private void processDataInMultipleThreads(final JobExecutionMultipleShardingContext shardingContext, final Map<Integer, List<T>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Entry<Integer, List<T>> each : data.entrySet()) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataList(shardingContext.createJobExecutionSingleShardingContext(each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        latchAwait(latch);
    }
    
    private void latchAwait(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processDataList(final JobExecutionSingleShardingContext singleContext, final List<T> data) {
        for (T each : data) {
            boolean isSuccess = false;
            try {
                isSuccess = processData(singleContext, each);
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            // CHECKSTYLE:ON
                ProcessCountStatistics.incrementProcessFailureCount(singleContext.getJobName());
                log.error("Elastic job: exception occur in job processing...", ex);
                continue;
            }
            if (isSuccess) {
                ProcessCountStatistics.incrementProcessSuccessCount(singleContext.getJobName());
            } else {
                ProcessCountStatistics.incrementProcessFailureCount(singleContext.getJobName());
            }
        }
    }
    
    /**
     * 获取待处理的数据.
     * 不返回空结果则永久不停止作业执行.
     * 
     * @param singleContext 作业分片规则配置上下文
     * @return 待处理的数据集合
     */
    protected abstract List<T> fetchData(final JobExecutionSingleShardingContext singleContext);
    
    
    /**
     * 处理数据.
     * 
     * @param singleContext 作业分片规则配置上下文
     * @param data 待处理的数据
     * @return 数据是否处理成功
     */
    protected abstract boolean processData(final JobExecutionSingleShardingContext singleContext, final T data);
}
