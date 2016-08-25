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
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.event.JobEventBus;
import com.dangdang.ddframe.job.event.JobTraceEvent;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * 简单作业执行器.
 * 
 * @author zhangliang
 */
public final class SimpleJobExecutor extends AbstractElasticJobExecutor {
    
    private final SimpleJob simpleJob;
    
    public SimpleJobExecutor(final SimpleJob simpleJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.simpleJob = simpleJob;
    }
    
    @Override
    protected void process(final ShardingContexts shardingContexts) {
        Collection<Integer> items = shardingContexts.getShardingItemParameters().keySet();
        if (1 == items.size()) {
            process(new ShardingContext(shardingContexts, shardingContexts.getShardingItemParameters().keySet().iterator().next()));
            return;
        }
        final CountDownLatch latch = new CountDownLatch(items.size());
        for (final int each : items) {
            getExecutorService().submit(new Runnable() {
            
                @Override
                public void run() {
                    try {
                        process(new ShardingContext(shardingContexts, each));
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
    
    private void process(final ShardingContext shardingContext) {
        try {
            simpleJob.execute(shardingContext);
            JobEventBus.getInstance().post(getJobName(), 
                    new JobTraceEvent(getJobName(), JobTraceEvent.LogLevel.TRACE, String.format("Execute simple job item is: '%s'.", shardingContext.getShardingItem())));
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            getJobExceptionHandler().handleException(getJobName(), ex);
        }
    }
}
