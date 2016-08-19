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
import com.dangdang.ddframe.job.api.executor.ShardingContexts;
import com.dangdang.ddframe.job.api.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;

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
public final class DataflowJobExecutor extends AbstractElasticJobExecutor {
    
    private final DataflowJob<Object> dataflowJob;
    
    public DataflowJobExecutor(final DataflowJob<Object> dataflowJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.dataflowJob = dataflowJob;
    }
    
    @Override
    protected void process(final ShardingContexts shardingContexts) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) getJobRootConfig().getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) {
            streamingExecute(shardingContexts);
        } else {
            oneOffExecute(shardingContexts);
        }
    }
    
    private void streamingExecute(final ShardingContexts shardingContexts) {
        Map<Integer, List<Object>> data = fetchData(shardingContexts);
        while (!data.isEmpty()) {
            processData(shardingContexts, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchData(shardingContexts);
        }
    }
    
    private void oneOffExecute(final ShardingContexts shardingContexts) {
        Map<Integer, List<Object>> data = fetchData(shardingContexts);
        if (!data.isEmpty()) {
            processData(shardingContexts, data);
        }
    }
    
    private Map<Integer, List<Object>> fetchData(final ShardingContexts shardingContexts) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        final Map<Integer, List<Object>> result = new ConcurrentHashMap<>(items.size(), 1);
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        List<Object> data = dataflowJob.fetchData(new ShardingContext(shardingContexts.getJobName(), 
                                shardingContexts.getShardingTotalCount(), shardingContexts.getJobParameter(), each, shardingContexts.getShardingItemParameters().get(each)));
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
        JobEventBus.getInstance().post(getJobName(), new JobTraceEvent(getJobName(), LogLevel.TRACE, String.format("Fetch data size: '%s'.", result.size())));
        return result;
    }
    
    private void processData(final ShardingContexts shardingContexts, final Map<Integer, List<Object>> data) {
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Map.Entry<Integer, List<Object>> each : data.entrySet()) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        dataflowJob.processData(new ShardingContext(shardingContexts.getJobName(), shardingContexts.getShardingTotalCount(), shardingContexts.getJobParameter(),
                                        each.getKey(), shardingContexts.getShardingItemParameters().get(each.getKey())), each.getValue());
                    // CHECKSTYLE:OFF
                    } catch (final Throwable cause) {
                    // CHECKSTYLE:ON
                        getJobExceptionHandler().handleException(getJobName(), cause);
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
}
