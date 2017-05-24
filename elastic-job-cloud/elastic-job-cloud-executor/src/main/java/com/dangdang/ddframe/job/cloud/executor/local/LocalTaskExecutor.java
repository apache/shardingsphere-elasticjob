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

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.cloud.executor.TaskExecutor;
import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.context.TaskContext;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import lombok.Builder;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mesos.Protos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.dangdang.ddframe.job.api.JobType.DATAFLOW;
import static com.dangdang.ddframe.job.api.JobType.SIMPLE;

/**
 * 本地作业执行器.
 * 
 * @author gaohongtao
 */
public final class LocalTaskExecutor {
    
    private static final Splitter SHARDING_ITEM_SPLITTER = Splitter.on("=").trimResults().omitEmptyStrings().limit(2);
    
    private final Class<? extends ElasticJob> jobClass;
    
    private final String beanName;
    
    private final String applicationContext;
    
    private final int shardingTotalCount;
    
    private final int shardingItem;
    
    private final String shardingParameter;
    
    private final String jobParameter;
    
    private final TaskContext taskContext;
    
    private final TaskExecutor taskExecutor = new TaskExecutor();
    
    private final ExecutorDriverMock executorDriverMock = new ExecutorDriverMock();
    
    @Builder
    private LocalTaskExecutor(final Class<? extends ElasticJob> jobClass, final String shardingItemExpression,
                              final String beanName, final String applicationContext, final String jobParameter, final int shardingTotalCount) {
        String requiredParameterErrMessage = "Required parameters: jobClass, shardingItemExpression";
        Preconditions.checkNotNull(jobClass, requiredParameterErrMessage);
        Preconditions.checkNotNull(shardingItemExpression, requiredParameterErrMessage);
        boolean springConfigExist = !Strings.isNullOrEmpty(beanName) && !Strings.isNullOrEmpty(applicationContext);
        boolean springConfigNotExist = Strings.isNullOrEmpty(beanName) && Strings.isNullOrEmpty(applicationContext);
        Preconditions.checkArgument(springConfigExist || springConfigNotExist, "beanName and applicationContext should exist at same time");
        this.jobClass = jobClass;
        this.beanName = Strings.nullToEmpty(beanName);
        this.applicationContext = Strings.nullToEmpty(applicationContext);
        Iterator<String> shardingItemIterator =  SHARDING_ITEM_SPLITTER.split(shardingItemExpression).iterator();
        shardingItem = Integer.valueOf(shardingItemIterator.next());
        if (shardingItemIterator.hasNext()) {
            shardingParameter = shardingItemIterator.next();
        } else {
            shardingParameter = "";
        }
        if (shardingTotalCount == 0) {
            this.shardingTotalCount = shardingItem + 1;
        } else {
            this.shardingTotalCount = shardingTotalCount;
        }
        this.jobParameter = Strings.nullToEmpty(jobParameter);
        taskContext = new TaskContext(jobClass.getSimpleName(), Collections.singletonList(shardingItem), ExecutionType.READY);
    }
    
    /**
     * 运行TRANSIENT类型任务.
     */
    public void runTransient() {
        run("");
    }
    
    private void run(final String cron) {
        executorDriverMock.setLastTaskState(Protos.TaskState.TASK_RUNNING);
        taskExecutor.launchTask(executorDriverMock, Protos.TaskInfo.newBuilder().setName(taskContext.getMetaInfo().getJobName())
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskContext.getId())).setSlaveId(Protos.SlaveID.newBuilder().setValue(taskContext.getSlaveId()))
                .setData(ByteString.copyFrom(serialize(cron))).build());
        while (executorDriverMock.getLastTaskState().equals(Protos.TaskState.TASK_RUNNING)) {
            Thread.yield();
        }
    }
    
    /**
     * 启动DAEMON类型任务.
     * 
     * @param cron cron表达式
     */
    public void runDaemon(final String cron) {
        run(cron);
    }
    
    /**
     * 停止作业执行.
     */
    public void stopDaemon() {
        taskExecutor.killTask(executorDriverMock, Protos.TaskID.newBuilder().setValue(taskContext.getId()).build());
    }
    
    private byte[] serialize(final String cron) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(2, 1);
        ShardingContexts shardingContexts = buildShardingContexts();
        result.put("shardingContext", shardingContexts);
        result.put("jobConfigContext", buildJobConfigurationContext(shardingContexts, cron));
        return SerializationUtils.serialize(result);
    }
    
    private ShardingContexts buildShardingContexts() {
        Map<Integer, String> shardingItemParameters = new HashMap<>(1);
        shardingItemParameters.put(shardingItem, shardingParameter);
        return new ShardingContexts(taskContext.getId(), taskContext.getMetaInfo().getJobName(), shardingTotalCount, jobParameter, shardingItemParameters, -1);
    }
    
    private Map<String, String> buildJobConfigurationContext(final ShardingContexts shardingContexts, final String cron) {
        Map<String, String> result = new LinkedHashMap<>(7);
        if (SimpleJob.class.isAssignableFrom(jobClass)) {
            result.put("jobType", SIMPLE.name());
        } else if (DataflowJob.class.isAssignableFrom(jobClass)) {
            result.put("jobType", DATAFLOW.name());
            result.put("streamingProcess", Boolean.FALSE.toString());
        } else {
            throw new IllegalArgumentException("Local task executor only support SIMPLE or DATAFLOW job");
        }
        result.put("jobName", shardingContexts.getJobName());
        result.put("jobClass", jobClass.getName());
        result.put("cron", cron);
        result.put("applicationContext", applicationContext);
        result.put("beanName", beanName);
        return result;
    }
}
