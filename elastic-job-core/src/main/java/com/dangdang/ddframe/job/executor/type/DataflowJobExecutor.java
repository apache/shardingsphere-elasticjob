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

package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.executor.JobFacade;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;

import java.util.Collection;
import java.util.HashMap;
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
        final Map<Integer, List<Object>> result;
        if (1 == items.size()) {
            result = fetchData(new ShardingContext(shardingContexts, shardingContexts.getShardingItemParameters().keySet().iterator().next()));
        } else {
            result = new ConcurrentHashMap<>(items.size(), 1);
            final CountDownLatch latch = new CountDownLatch(items.size());
            for (final int each : items) {
                getExecutorService().submit(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            result.putAll(fetchData(new ShardingContext(shardingContexts, each)));
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
        JobEventBus.getInstance().post(getJobName(), new JobTraceEvent(getJobName(), LogLevel.TRACE, String.format("Fetch data size: '%s', '%s' items has data.", result.size(), result.keySet())));
        return result;
    }
    
    private Map<Integer, List<Object>> fetchData(final ShardingContext shardingContext) {
        Map<Integer, List<Object>> result = new HashMap<>(1, 1);
        try {
            List<Object> data = dataflowJob.fetchData(shardingContext);
            if (null != data && !data.isEmpty()) {
                result.put(shardingContext.getShardingItem(), data);
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            getJobExceptionHandler().handleException(getJobName(), cause);
        }
        return result;
    }
    
    private void processData(final ShardingContexts shardingContexts, final Map<Integer, List<Object>> data) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        if (1 == items.size()) {
            int item = shardingContexts.getShardingItemParameters().keySet().iterator().next();
            processData(new ShardingContext(shardingContexts, item), data.get(item));
            return;
        }
        final CountDownLatch latch = new CountDownLatch(data.size());
        for (final Map.Entry<Integer, List<Object>> each : data.entrySet()) {
            getExecutorService().submit(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        processData(new ShardingContext(shardingContexts, each.getKey()), each.getValue());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        JobEventBus.getInstance().post(getJobName(), new JobTraceEvent(getJobName(), LogLevel.TRACE, String.format("Process data size: '%s'.", data.size())));
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processData(final ShardingContext shardingContext, final List<Object> data) {
        try {
            dataflowJob.processData(shardingContext, data);
            // CHECKSTYLE:OFF
        } catch (final Throwable cause) {
            // CHECKSTYLE:ON
            getJobExceptionHandler().handleException(getJobName(), cause);
        }
    }
}
