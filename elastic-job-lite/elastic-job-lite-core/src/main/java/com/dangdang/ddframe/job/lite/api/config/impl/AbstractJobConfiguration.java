/*
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
 *
 */

package com.dangdang.ddframe.job.lite.api.config.impl;

import com.dangdang.ddframe.job.lite.api.ElasticJob;
import com.dangdang.ddframe.job.lite.api.config.JobConfiguration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 基本作业配置信息.
 * 
 * @author caohao
 */
@Getter
@AllArgsConstructor
public abstract class AbstractJobConfiguration<T extends ElasticJob> implements JobConfiguration<T> {
    
    private final String jobName;
    
    private final JobType jobType;
    
    private final Class<? extends T> jobClass;
    
    private final int shardingTotalCount;
    
    private final String cron;
    
    private final String shardingItemParameters;
    
    private final String jobParameter;
    
    private final boolean monitorExecution;
    
    private final int maxTimeDiffSeconds;
    
    private final boolean failover;
    
    private final boolean misfire;
    
    private final int monitorPort;
    
    private final String jobShardingStrategyClass;
    
    private final String description;
    
    private final boolean disabled;
    
    private final boolean overwrite;
    
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    public abstract static class AbstractJobConfigurationBuilder<T extends AbstractJobConfiguration, J extends ElasticJob, B extends AbstractJobConfigurationBuilder> {
        
        private final String jobName;
        
        private final JobType jobType;
        
        private final Class<? extends J> jobClass;
        
        private final int shardingTotalCount;
        
        private final String cron;
    
        private String shardingItemParameters = "";
    
        private String jobParameter = "";
    
        private boolean monitorExecution = true;
    
        private int maxTimeDiffSeconds = -1;
    
        private boolean failover;
    
        private boolean misfire = true;
    
        private int monitorPort = -1;
    
        private String jobShardingStrategyClass = "";
    
        private String description = "";
    
        private boolean disabled;
    
        private boolean overwrite;
        
        /**
         * 设置分片序列号和个性化参数对照表.
         *
         * <p>
         * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map.
         * 分片序列号从0开始, 不可大于或等于作业分片总数.
         * 如:
         * 0=a,1=b,2=c
         * </p>
         *
         * @param shardingItemParameters 分片序列号和个性化参数对照表
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B shardingItemParameters(final String shardingItemParameters) {
            this.shardingItemParameters = shardingItemParameters;
            return (B) this;
        }
        
        /**
         * 设置作业自定义参数.
         *
         * <p>
         * 可以配置多个相同的作业, 但是用不同的参数作为不同的调度实例.
         * </p>
         *
         * @param jobParameter 作业自定义参数
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B jobParameter(final String jobParameter) {
            this.jobParameter = jobParameter;
            return (B) this;
        }
        
        /**
         * 设置监控作业执行时状态.
         *
         * <p>
         * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.
         * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
         * </p>
         *
         * @param monitorExecution 监控作业执行时状态
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B monitorExecution(final boolean monitorExecution) {
            this.monitorExecution = monitorExecution;
            return (B) this;
        }
        
        /**
         * 设置最大容忍的本机与注册中心的时间误差秒数.
         *
         * <p>
         * 如果时间误差超过配置秒数则作业启动时将抛异常.
         * 配置为-1表示不检查时间误差.
         * </p>
         *
         * @param maxTimeDiffSeconds 最大容忍的本机与注册中心的时间误差秒数
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B maxTimeDiffSeconds(final int maxTimeDiffSeconds) {
            this.maxTimeDiffSeconds = maxTimeDiffSeconds;
            return (B) this;
        }
        
        /**
         * 设置是否开启失效转移.
         *
         * <p>
         * 只有对monitorExecution的情况下才可以开启失效转移.
         * </p> 
         *
         * @param failover 是否开启失效转移
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B failover(final boolean failover) {
            this.failover = failover;
            return (B) this;
        }
        
        /**
         * 设置是否开启misfire.
         * 
         * @param misfire 是否开启misfire
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B misfire(final boolean misfire) {
            this.misfire = misfire;
            return (B) this;
        }
        
        /**
         * 设置作业辅助监控端口.
         *
         * @param monitorPort 作业辅助监控端口
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B monitorPort(final int monitorPort) {
            this.monitorPort = monitorPort;
            return (B) this;
        }
        
        /**
         * 设置作业分片策略实现类全路径.
         *
         * <p>
         * 默认使用{@code com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy}.
         * </p>
         *
         * @param jobShardingStrategyClass 作业辅助监控端口
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B jobShardingStrategyClass(final String jobShardingStrategyClass) {
            this.jobShardingStrategyClass = jobShardingStrategyClass;
            return (B) this;
        }
        
        /**
         * 设置作业描述信息.
         *
         * @param description 作业描述信息
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B description(final String description) {
            this.description = description;
            return (B) this;
        }
        
        /**
         * 设置作业是否禁止启动.
         * 
         * <p>
         * 可用于部署作业时, 先禁止启动, 部署结束后统一启动.
         * </p>
         *
         * @param disabled 作业是否禁止启动
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B disabled(final boolean disabled) {
            this.disabled = disabled;
            return (B) this;
        }
        
        /**
         * 设置本地配置是否可覆盖注册中心配置.
         * 
         * <p>
         * 如果可覆盖, 每次启动作业都以本地配置为准.
         * </p>
         *
         * @param overwrite 本地配置是否可覆盖注册中心配置
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B overwrite(final boolean overwrite) {
            this.overwrite = overwrite;
            return (B) this;
        }
        
        /**
         * 构建作业配置对象.
         * 
         * @return 作业配置对象
         */
        public final T build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
            Preconditions.checkArgument(ElasticJob.class.isAssignableFrom(jobClass), "job class should be an instance of ElasticJob.");
            Preconditions.checkArgument(shardingTotalCount > 0, "shardingTotalCount should larger than zero.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cron), "cron can not be empty.");
            Preconditions.checkArgument(null != shardingItemParameters, "shardingItemParameters can not be null.");
            Preconditions.checkArgument(null != jobParameter, "jobParameter can not be null.");
            Preconditions.checkArgument(null != jobShardingStrategyClass, "jobShardingStrategyClass can not be null.");
            Preconditions.checkArgument(null != description, "description can not be null.");
            return buildInternal();
        }
        
        protected abstract T buildInternal();
    }
}
