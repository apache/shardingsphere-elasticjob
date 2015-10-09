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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import com.dangdang.ddframe.job.internal.statistics.ProcessCountStatistics;
import com.google.common.collect.Lists;

/**
 * 不断获取并处理最新数据的分布式作业的基类.
 * 
 * @author zhangliang
 * 
 * @param <T> 执行作业的实体类型
 */
@Slf4j
public abstract class AbstractPerpetualElasticJob<T> extends AbstractElasticJob {
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Override
    protected final void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        int threadCount = getConfigService().getConcurrentDataProcessThreadCount();
        List<T> data = fetchData(shardingContext);
        log.debug("Elastic job: perpetual elastic job fetch data size: {}.", data != null ? data.size() : 0);
        while (null != data && !data.isEmpty() && !isStoped() && !getShardingService().isNeedSharding()) {
            if (threadCount <= 1 || data.size() <= threadCount) {
                processDataList(shardingContext, data);
            } else {
                processDataInMultipleThreads(shardingContext, threadCount, data);
            }
            data = fetchData(shardingContext);
            log.debug("Elastic job: perpetual elasticJob fetch data size: {}.", data != null ? data.size() : 0);
        }
    }
    
    private void processDataInMultipleThreads(final JobExecutionMultipleShardingContext shardingContext, final int threadCount, final List<T> data) {
        List<List<T>> splitedData = Lists.partition(data, data.size() / threadCount);
        final CountDownLatch latch = new CountDownLatch(splitedData.size());
        for (final List<T> each : splitedData) {
            executorService.submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processDataList(shardingContext, each);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processDataList(final JobExecutionMultipleShardingContext shardingContext, final List<T> data) {
        for (T each : data) {
            boolean isSuccess = false;
            try {
                isSuccess = processData(shardingContext, each);
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            // CHECKSTYLE:ON
                ProcessCountStatistics.incrementProcessFailureCount(shardingContext.getJobName());
                log.error("Elastic job: exception occur in job processing...", ex);
                continue;
            }
            if (isSuccess) {
                ProcessCountStatistics.incrementProcessSuccessCount(shardingContext.getJobName());
            } else {
                ProcessCountStatistics.incrementProcessFailureCount(shardingContext.getJobName());
            }
        }
    }
    
    /**
     * 获取待处理的数据.
     * 不返回空结果则永久不停止作业执行.
     * 
     * @param shardingContext 作业分片规则配置上下文
     * @return 待处理的数据集合
     */
    protected abstract List<T> fetchData(final JobExecutionMultipleShardingContext shardingContext);
    
    /**
     * 处理数据.
     * 
     * @param shardingContext 作业分片规则配置上下文
     * @param data 待处理的数据
     * @return 数据是否处理成功
     */
    protected abstract boolean processData(final JobExecutionMultipleShardingContext shardingContext, final T data);
    
    /**
     * 更新数据处理位置.
     * 
     * @param item 分片项
     * @param offset 数据处理位置
     */
    protected void updateOffset(final int item, final String offset) {
        getOffsetService().updateOffset(item, offset);
    }
}
