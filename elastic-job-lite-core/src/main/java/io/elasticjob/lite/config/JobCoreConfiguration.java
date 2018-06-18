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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.elasticjob.lite.executor.handler.JobProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 作业核心配置.
 * 
 * @author zhangliang
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class JobCoreConfiguration {
    
    private final String jobName;
    
    private final String cron;
    
    private final int shardingTotalCount;
    
    private final String shardingItemParameters;
    
    private final String jobParameter;
    
    private final boolean failover;
    
    private final boolean misfire;
    
    private final String description;
    
    private final JobProperties jobProperties;
    
    /**
     * 创建简单作业配置构建器.
     *
     * @param jobName 作业名称
     * @param cron 作业启动时间的cron表达式
     * @param shardingTotalCount 作业分片总数
     * @return 简单作业配置构建器
     */
    public static Builder newBuilder(final String jobName, final String cron, final int shardingTotalCount) {
        return new Builder(jobName, cron, shardingTotalCount);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        
        private final String jobName;
        
        private final String cron;
        
        private final int shardingTotalCount;
        
        private String shardingItemParameters = "";
        
        private String jobParameter = "";
        
        private boolean failover;
        
        private boolean misfire = true;
        
        private String description = "";
        
        private final JobProperties jobProperties = new JobProperties();
        
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
        public Builder shardingItemParameters(final String shardingItemParameters) {
            if (null != shardingItemParameters) {
                this.shardingItemParameters = shardingItemParameters;
            }
            return this;
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
        public Builder jobParameter(final String jobParameter) {
            if (null != jobParameter) {
                this.jobParameter = jobParameter;
            }
            return this;
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
        public Builder failover(final boolean failover) {
            this.failover = failover;
            return this;
        }
        
        /**
         * 设置是否开启misfire.
         *
         * @param misfire 是否开启misfire
         *
         * @return 作业配置构建器
         */
        public Builder misfire(final boolean misfire) {
            this.misfire = misfire;
            return this;
        }
        
        /**
         * 设置作业描述信息.
         *
         * @param description 作业描述信息
         *
         * @return 作业配置构建器
         */
        public Builder description(final String description) {
            if (null != description) {
                this.description = description;
            }
            return this;
        }
        
        /**
         * 设置作业属性.
         *
         * @param key 属性键
         * @param value 属性值
         *
         * @return 作业配置构建器
         */
        public Builder jobProperties(final String key, final String value) {
            jobProperties.put(key, value);
            return this;
        }
        
        /**
         * 构建作业配置对象.
         *
         * @return 作业配置对象
         */
        public final JobCoreConfiguration build() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(jobName), "jobName can not be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(cron), "cron can not be empty.");
            Preconditions.checkArgument(shardingTotalCount > 0, "shardingTotalCount should larger than zero.");
            return new JobCoreConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters, jobParameter, failover, misfire, description, jobProperties);
        }
    }
}
