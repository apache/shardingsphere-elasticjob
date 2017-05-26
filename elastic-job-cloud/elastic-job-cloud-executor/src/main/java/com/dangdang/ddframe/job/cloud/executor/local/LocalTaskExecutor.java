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

package com.dangdang.ddframe.job.cloud.executor.local;

import com.dangdang.ddframe.job.cloud.executor.TaskExecutor;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.util.config.ShardingItemParameters;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.dangdang.ddframe.job.api.JobType.DATAFLOW;
import static com.dangdang.ddframe.job.cloud.executor.local.LocalCloudJobExecutionType.DAEMON;

/**
 * 本地作业执行器.
 * 
 * @author gaohongtao
 */
public final class LocalTaskExecutor {
    
    private final TaskExecutor taskExecutor = new TaskExecutor();
    
    private final LocalExecutorDriver localExecutorDriver;
    
    private final LocalCloudJobConfiguration localCloudJobConfiguration;
    
    private final CountDownLatch latch;
    
    private final int shardingTotalCount;
    
    private final List<Protos.TaskID> runningTasks;
    
    public LocalTaskExecutor(final LocalCloudJobConfiguration localCloudJobConfiguration) {
        this.localCloudJobConfiguration = localCloudJobConfiguration;
        shardingTotalCount = localCloudJobConfiguration.getTypeConfig().getCoreConfig().getShardingTotalCount();
        latch = new CountDownLatch(shardingTotalCount);
        localExecutorDriver = new LocalExecutorDriver(latch);
        runningTasks = new ArrayList<>(shardingTotalCount);
    }
    
    /**
     * 运行作业.
     */
    public Future<Integer> run() {
        Map<Integer, String> shardingItemParameters = new ShardingItemParameters(localCloudJobConfiguration.getTypeConfig().getCoreConfig().getShardingItemParameters()).getMap();
        for (int i = 0; i < shardingTotalCount; i++) {
            TaskContext taskContext = new TaskContext(localCloudJobConfiguration.getJobName(), Collections.singletonList(i), ExecutionType.READY);
            Protos.TaskID taskID = Protos.TaskID.newBuilder().setValue(taskContext.getId()).build();
            runningTasks.add(taskID);
            taskExecutor.launchTask(localExecutorDriver, Protos.TaskInfo.newBuilder().setName(localCloudJobConfiguration.getJobName())
                    .setTaskId(taskID).setSlaveId(Protos.SlaveID.newBuilder().setValue(taskContext.getSlaveId()))
                    .setData(ByteString.copyFrom(serialize(taskContext, i, shardingItemParameters.get(i)))).build());
        }
        return new ExecutorFuture();
    }
    
    private byte[] serialize(final TaskContext taskContext, final Integer shardingItem, final String shardingParameter) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("shardingContext", buildShardingContexts(taskContext, shardingItem, shardingParameter));
        result.put("jobConfigContext", buildJobConfigurationContext());
        return SerializationUtils.serialize(result);
    }
    
    private ShardingContexts buildShardingContexts(final TaskContext taskContext, final Integer shardingItem, final String shardingParameter) {
        Map<Integer, String> shardingItemParameters = new HashMap<>(1);
        shardingItemParameters.put(shardingItem, shardingParameter);
        return new ShardingContexts(taskContext.getId(), taskContext.getMetaInfo().getJobName(), shardingTotalCount, localCloudJobConfiguration
                .getTypeConfig().getCoreConfig().getJobParameter(), shardingItemParameters, -1);
    }
    
    private Map<String, String> buildJobConfigurationContext() {
        Map<String, String> result = new LinkedHashMap<>(7);
        if (localCloudJobConfiguration.getTypeConfig().getJobType().equals(DATAFLOW)) {
            result.put("streamingProcess", Boolean.toString(((DataflowJobConfiguration) localCloudJobConfiguration.getTypeConfig()).isStreamingProcess()));
        }
        result.put("jobType", localCloudJobConfiguration.getTypeConfig().getJobType().name());
        result.put("jobName", localCloudJobConfiguration.getJobName());
        result.put("jobClass", localCloudJobConfiguration.getTypeConfig().getJobClass());
        if (DAEMON.equals(localCloudJobConfiguration.getExecutionType())) {
            result.put("cron", localCloudJobConfiguration.getTypeConfig().getCoreConfig().getCron());
        }
        result.put("applicationContext", localCloudJobConfiguration.getApplicationContext());
        result.put("beanName", localCloudJobConfiguration.getBeanName());
        return result;
    }
    
    private class ExecutorFuture implements Future<Integer> {
        
        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            for (Protos.TaskID each : runningTasks) {
                taskExecutor.killTask(localExecutorDriver, each);
            }
            return true;
        }
        
        @Override
        public boolean isCancelled() {
            return DAEMON.equals(localCloudJobConfiguration.getExecutionType());
        }
        
        @Override
        public boolean isDone() {
            return latch.getCount() < 1;
        }
    
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            latch.await();
            return Long.valueOf(shardingTotalCount - latch.getCount()).intValue();
        }
        
        @Override
        public Integer get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            latch.await(timeout, unit);
            return Long.valueOf(shardingTotalCount - latch.getCount()).intValue();
        }
        
    }
}
