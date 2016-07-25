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

package com.dangdang.ddframe.job.lite.api.config.impl;

import com.dangdang.ddframe.job.api.dataflow.DataflowElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowType;
import com.google.common.base.Preconditions;
import lombok.Getter;

/**
 * 数据流作业配置信息.
 * 
 * @author caohao
 */
@Getter
public final class DataflowJobConfiguration<T extends DataflowElasticJob> extends AbstractJobConfiguration<T> {
    
    private final DataflowType dataflowType;
    
    private final int processCountIntervalSeconds;
    
    private final int concurrentDataProcessThreadCount;
    
    private final boolean streamingProcess;
    
    //CHECKSTYLE:OFF
    private DataflowJobConfiguration(final String jobName, final Class<? extends T> jobClass, final int shardingTotalCount, final String cron,
                                     final String shardingItemParameters, final String jobParameter, final boolean monitorExecution, final int maxTimeDiffSeconds,
                                     final boolean isFailover, final boolean isMisfire, final int monitorPort, final String jobShardingStrategyClass, final String description,
                                     final boolean disabled, final boolean overwrite, final DataflowType dataflowType, final int processCountIntervalSeconds, final int concurrentDataProcessThreadCount,
                                     final boolean streamingProcess) {
        //CHECKSTYLE:ON
        super(jobName, JobType.DATAFLOW, jobClass, shardingTotalCount, cron, shardingItemParameters, jobParameter, monitorExecution, maxTimeDiffSeconds, isFailover, isMisfire,
                monitorPort, jobShardingStrategyClass, description, disabled, overwrite);
        this.dataflowType = dataflowType;
        this.processCountIntervalSeconds = processCountIntervalSeconds;
        this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
        this.streamingProcess = streamingProcess;
    }
    
    
    public static class DataflowJobConfigurationBuilder extends AbstractJobConfigurationBuilder<DataflowJobConfiguration, DataflowElasticJob, DataflowJobConfigurationBuilder> {
    
        private final DataflowType dataflowType;
        
        private int processCountIntervalSeconds = 300;
        
        private int concurrentDataProcessThreadCount = Runtime.getRuntime().availableProcessors() * 2;
        
        private boolean streamingProcess;
        
        public DataflowJobConfigurationBuilder(
                final String jobName, final Class<? extends DataflowElasticJob> jobClass, final int shardingTotalCount, final String cron, final DataflowType dataflowType) {
            super(jobName, JobType.DATAFLOW, jobClass, shardingTotalCount, cron);
            this.dataflowType = dataflowType;
        }
        
        /**
         * 设置统计作业处理数据数量的间隔时间.
         *
         * <p>
         * 单位：秒, 不能小于1.
         * </p>
         *
         * @param processCountIntervalSeconds 统计作业处理数据数量的间隔时间
         *
         * @return 作业配置构建器
         */
        public final DataflowJobConfigurationBuilder processCountIntervalSeconds(final int processCountIntervalSeconds) {
            this.processCountIntervalSeconds = processCountIntervalSeconds;
            return this;
        }
        
        /**
         * 设置同时处理数据的并发线程数.
         *
         * <p>
         * 不能小于1.
         * </p>
         *
         * @param concurrentDataProcessThreadCount 同时处理数据的并发线程数
         *
         * @return 作业配置构建器
         */
        public final DataflowJobConfigurationBuilder concurrentDataProcessThreadCount(final int concurrentDataProcessThreadCount) {
            this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
            return this;
        }
        
        /**
         * 设置是否流式处理数据.
         * 
         * <p>
         * 如果流式处理数据, 则fetchData不返回空结果将持续执行作业. 如果非流式处理数据, 则处理数据完成后作业结束.
         * </p>
         *
         * @param streamingProcess 是否流式处理数据
         * 
         * @return 作业配置构建器
         */
        public final DataflowJobConfigurationBuilder streamingProcess(final boolean streamingProcess) {
            this.streamingProcess = streamingProcess;
            return this;
        }
        
        @SuppressWarnings("unchecked")
        protected DataflowJobConfiguration buildInternal() {
            Preconditions.checkArgument(processCountIntervalSeconds > 0, String.format("%d should larger than zero.", processCountIntervalSeconds));
            Preconditions.checkArgument(concurrentDataProcessThreadCount > 0, String.format("%d should larger than zero.", concurrentDataProcessThreadCount));
            return new DataflowJobConfiguration(getJobName(), getJobClass(), getShardingTotalCount(), getCron(), getShardingItemParameters(), getJobParameter(),
                    isMonitorExecution(), getMaxTimeDiffSeconds(), isFailover(), isMisfire(), getMonitorPort(), getJobShardingStrategyClass(),
                    getDescription(), isDisabled(), isOverwrite(), dataflowType, processCountIntervalSeconds, concurrentDataProcessThreadCount, streamingProcess);
        }
    }
}
