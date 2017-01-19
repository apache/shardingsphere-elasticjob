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
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.executor.JobFacade;

import java.util.List;
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
    protected void process(final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) getJobRootConfig().getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) {
            streamingExecute(shardingContext, dataflowConfig.getProcessDataThreadCount());
        } else {
            oneOffExecute(shardingContext, dataflowConfig.getProcessDataThreadCount());
        }
    }
    
    private void streamingExecute(final ShardingContext shardingContext, final int processDataThreadCount) {
        List<Object> data = fetchData(shardingContext);
        while (null != data && !data.isEmpty()) {
            concurrentProcessData(shardingContext, processDataThreadCount, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchData(shardingContext);
        }
    }
    
    private void oneOffExecute(final ShardingContext shardingContext, final int processDataThreadCount) {
        List<Object> data = fetchData(shardingContext);
        if (null != data && !data.isEmpty()) {
            concurrentProcessData(shardingContext, processDataThreadCount, data);
        }
    }

    private void concurrentProcessData(final ShardingContext shardingContext, int processDataThreadCount, List<Object> data) {
        if(1 == processDataThreadCount) {
            processData(shardingContext, data);
            return;
        }
        final CountDownLatch latch = new CountDownLatch(processDataThreadCount);
        int total = data.size();
        int dataSizeOfPerThread = total % processDataThreadCount == 0 ? total / processDataThreadCount : total / processDataThreadCount + 1;
        for(int i = 0; i < processDataThreadCount;i++) {
			int startIndex = i * dataSizeOfPerThread;
			int endIndex = (i + 1) * dataSizeOfPerThread > total ? total : (i + 1) * dataSizeOfPerThread;
			final List<Object> eachData = data.subList(startIndex, endIndex);

			getExecutorService().submit(new Runnable() {
				@Override
				public void run() {
					try {
						processData(shardingContext, eachData);
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

    private List<Object> fetchData(final ShardingContext shardingContext) {
        return dataflowJob.fetchData(shardingContext);
    }
    
    private void processData(final ShardingContext shardingContext, final List<Object> data) {
        dataflowJob.processData(shardingContext, data);
    }
}
