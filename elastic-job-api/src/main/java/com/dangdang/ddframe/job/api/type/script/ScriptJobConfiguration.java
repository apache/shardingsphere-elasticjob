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

package com.dangdang.ddframe.job.api.type.script;

import com.dangdang.ddframe.job.api.ScriptElasticJob;
import com.dangdang.ddframe.job.api.internal.config.AbstractJobConfiguration;
import com.dangdang.ddframe.job.api.internal.config.JobType;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

/**
 * 脚本作业配置信息.
 * 
 * @author caohao
 */
@Getter
public final class ScriptJobConfiguration extends AbstractJobConfiguration<ScriptElasticJob> {
    
    private final String scriptCommandLine;
    
    //CHECKSTYLE:OFF
    private ScriptJobConfiguration(final String jobName, final String cron, final int shardingTotalCount, final String shardingItemParameters, 
                                   final String jobParameter, final Boolean isFailover, final Boolean isMisfire, final String description, final String scriptCommandLine) {
    //CHECKSTYLE:ON
        super(jobName, JobType.SCRIPT, ScriptElasticJob.class, cron, shardingTotalCount, shardingItemParameters, jobParameter, isFailover, isMisfire, description);
        this.scriptCommandLine = scriptCommandLine;
    }
    
    public static class ScriptJobConfigurationBuilder extends AbstractJobConfigurationBuilder<ScriptJobConfiguration, ScriptElasticJob, ScriptJobConfigurationBuilder> {
        
        private String scriptCommandLine;
        
        public ScriptJobConfigurationBuilder(final String jobName, final String cron, final int shardingTotalCount, final String scriptCommandLine) {
            super(jobName, JobType.SCRIPT, ScriptElasticJob.class, cron, shardingTotalCount);
            this.scriptCommandLine = scriptCommandLine;
        }
        
        /**
         * 设置作业执行命令行.
         *
         * @param scriptCommandLine 作业执行命令行
         *
         * @return 作业配置构建器
         */
        public ScriptJobConfigurationBuilder scriptCommandLine(final String scriptCommandLine) {
            this.scriptCommandLine = scriptCommandLine;
            return this;
        }
        
        @Override
        protected ScriptJobConfiguration buildInternal() {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(scriptCommandLine), "script command line can not be empty.");
            return new ScriptJobConfiguration(
                    getJobName(), getCron(), getShardingTotalCount(), getShardingItemParameters(), getJobParameter(), isFailover(), isMisfire(), getDescription(), scriptCommandLine);
        }
    }
}
