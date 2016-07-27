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

package com.dangdang.ddframe.job.api.internal.config;

import com.dangdang.ddframe.job.api.internal.ElasticJob;
import com.dangdang.ddframe.job.api.JobConfiguration;
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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractJobConfiguration<T extends ElasticJob> implements JobConfiguration<T> {
    
    private final String jobName;
    
    private final JobType jobType;
    
    private final Class<? extends T> jobClass;
    
    private final String cron;
    
    private final int shardingTotalCount;
    
    private final String shardingItemParameters;
    
    private final String jobParameter;
    
    private final boolean failover;
    
    private final boolean misfire;
    
    private final String description;
    
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    public abstract static class Builder<T extends AbstractJobConfiguration, J extends ElasticJob, B extends Builder> {
        
        private final String jobName;
        
        private final JobType jobType;
        
        private final Class<? extends J> jobClass;
        
        private final String cron;
        
        private final int shardingTotalCount;
        
        private String shardingItemParameters = "";
    
        private String jobParameter = "";
    
        private boolean failover;
    
        private boolean misfire = true;
    
        private String description = "";
        
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
            if (null != shardingItemParameters) {
                this.shardingItemParameters = shardingItemParameters;
            }
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
            if (null != jobParameter) {
                this.jobParameter = jobParameter;
            }
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
         * 设置作业描述信息.
         *
         * @param description 作业描述信息
         *
         * @return 作业配置构建器
         */
        @SuppressWarnings("unchecked")
        public B description(final String description) {
            if (null != description) {
                this.description = description;
            }
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
            return buildInternal();
        }
        
        protected abstract T buildInternal();
    }
}
