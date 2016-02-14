/**
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
 */

package com.dangdang.ddframe.job.plugin.job.type.simple;

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionException;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;

/**
 * 简单的分布式作业.
 * 
 * <p>
 * 仅保证作业可被分布式定时调用, 不提供任何作业处理逻辑.
 * </p>
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public abstract class AbstractSimpleElasticJob extends AbstractElasticJob {
    
    @Override
    protected final void executeJob(final JobExecutionMultipleShardingContext shardingContext) {
        process(shardingContext);
    }
    
    @Override
    public void handleJobExecutionException(final JobExecutionException jobExecutionException) throws JobExecutionException {
        log.error("Elastic job: exception occur in job processing...", jobExecutionException.getCause());
    }
    
    /**
     * 执行作业.
     * 
     * @param shardingContext 作业分片规则配置上下文
     */
    public abstract void process(final JobExecutionMultipleShardingContext shardingContext);
}
