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

package io.elasticjob.lite.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lite作业配置.
 * 
 * @author caohao
 * @author zhangliang
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteJobConfiguration implements JobRootConfiguration {
    
    private final JobTypeConfiguration typeConfig;
    
    private final boolean monitorExecution;
    
    private final int maxTimeDiffSeconds;
    
    private final int monitorPort;
    
    private final String jobShardingStrategyClass;
    
    private final int reconcileIntervalMinutes;
    
    private final boolean disabled;
    
    private final boolean overwrite;
    
    /**
     * 获取作业名称.
     * 
     * @return 作业名称
     */
    public String getJobName() {
        return typeConfig.getCoreConfig().getJobName();
    }
    
    /**
     * 获取是否开启失效转移.
     *
     * @return 是否开启失效转移
     */
    public boolean isFailover() {
        return typeConfig.getCoreConfig().isFailover();
    }
    
    /**
     * 创建Lite作业配置构建器.
     * 
     * @param jobConfig 作业配置
     * @return Lite作业配置构建器
     */
    public static Builder newBuilder(final JobTypeConfiguration jobConfig) {
        return new Builder(jobConfig);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        
        private final JobTypeConfiguration jobConfig;
    
        private boolean monitorExecution = true;
        
        private int maxTimeDiffSeconds = -1;
        
        private int monitorPort = -1;
        
        private String jobShardingStrategyClass = "";
        
        private boolean disabled;
        
        private boolean overwrite;
        
        private int reconcileIntervalMinutes = 10;
    
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
        public Builder monitorExecution(final boolean monitorExecution) {
            this.monitorExecution = monitorExecution;
            return this;
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
        public Builder maxTimeDiffSeconds(final int maxTimeDiffSeconds) {
            this.maxTimeDiffSeconds = maxTimeDiffSeconds;
            return this;
        }
        
        /**
         * 设置作业辅助监控端口.
         *
         * @param monitorPort 作业辅助监控端口
         *
         * @return 作业配置构建器
         */
        public Builder monitorPort(final int monitorPort) {
            this.monitorPort = monitorPort;
            return this;
        }
        
        /**
         * 设置作业分片策略实现类全路径.
         *
         * <p>
         * 默认使用{@code io.elasticjob.lite.api.strategy.impl.AverageAllocationJobShardingStrategy}.
         * </p>
         *
         * @param jobShardingStrategyClass 作业分片策略实现类全路径
         *
         * @return 作业配置构建器
         */
        public Builder jobShardingStrategyClass(final String jobShardingStrategyClass) {
            if (null != jobShardingStrategyClass) {
                this.jobShardingStrategyClass = jobShardingStrategyClass;
            }
            return this;
        }
    
        /**
         * 设置修复作业服务器不一致状态服务执行间隔分钟数.
         *
         * <p>
         * 每隔一段时间监视作业服务器的状态，如果不正确则重新分片.
         * </p>
         *
         * @param reconcileIntervalMinutes 修复作业服务器不一致状态服务执行间隔分钟数
         *
         * @return 作业配置构建器
         */
        public Builder reconcileIntervalMinutes(final int reconcileIntervalMinutes) {
            this.reconcileIntervalMinutes = reconcileIntervalMinutes;
            return this;
        }
        
        /**
         * 设置作业是否启动时禁止.
         * 
         * <p>
         * 可用于部署作业时, 先在启动时禁止, 部署结束后统一启动.
         * </p>
         *
         * @param disabled 作业是否启动时禁止
         *
         * @return 作业配置构建器
         */
        public Builder disabled(final boolean disabled) {
            this.disabled = disabled;
            return this;
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
        public Builder overwrite(final boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }
        
        /**
         * 构建作业配置对象.
         * 
         * @return 作业配置对象
         */
        public final LiteJobConfiguration build() {
            return new LiteJobConfiguration(jobConfig, monitorExecution, maxTimeDiffSeconds, monitorPort, jobShardingStrategyClass, reconcileIntervalMinutes, disabled, overwrite);
        }
    }
}
