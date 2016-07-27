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

package com.dangdang.ddframe.job.api.type.dataflow;

import com.dangdang.ddframe.job.api.DataflowElasticJob;
import com.dangdang.ddframe.job.api.internal.config.JobType;
import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfiguration;
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
    
    private final boolean streamingProcess;
    
    private final int concurrentDataProcessThreadCount;
    
    //CHECKSTYLE:OFF
    private DataflowJobConfiguration(final String jobName, final Class<? extends T> jobClass, final String cron, final int shardingTotalCount, final String shardingItemParameters, 
                                     final String jobParameter, final boolean isFailover, final boolean isMisfire, final String description, 
                                     final DataflowType dataflowType, final boolean streamingProcess, final int concurrentDataProcessThreadCount) {
    //CHECKSTYLE:ON
        super(jobName, JobType.DATAFLOW, jobClass, cron, shardingTotalCount, shardingItemParameters, jobParameter, isFailover, isMisfire, description);
        this.dataflowType = dataflowType;
        this.streamingProcess = streamingProcess;
        this.concurrentDataProcessThreadCount = concurrentDataProcessThreadCount;
    }
    
    /**
     * 数据流作业支持的处理类型.
     *
     * @author zhangliang
     */
    public enum DataflowType {
        
        THROUGHPUT,
        SEQUENCE
    }
    
    public static class DataflowJobConfigurationBuilder extends Builder<DataflowJobConfiguration, DataflowElasticJob, DataflowJobConfigurationBuilder> {
        
        private final DataflowType dataflowType;
        
        private int concurrentDataProcessThreadCount = Runtime.getRuntime().availableProcessors() * 2;
        
        private boolean streamingProcess;
        
        public DataflowJobConfigurationBuilder(
                final String jobName, final Class<? extends DataflowElasticJob> jobClass, final String cron, final int shardingTotalCount, final DataflowType dataflowType) {
            super(jobName, JobType.DATAFLOW, jobClass, cron, shardingTotalCount);
            this.dataflowType = dataflowType;
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
            Preconditions.checkArgument(concurrentDataProcessThreadCount > 0, String.format("%d should larger than zero.", concurrentDataProcessThreadCount));
            return new DataflowJobConfiguration(getJobName(), getJobClass(), getCron(), getShardingTotalCount(), getShardingItemParameters(), getJobParameter(), isFailover(), isMisfire(), 
                    getDescription(), dataflowType, streamingProcess, concurrentDataProcessThreadCount);
        }
    }
}
