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

package com.dangdang.ddframe.job.api.simple;

import com.dangdang.ddframe.job.api.internal.JobFacade;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.internal.AbstractElasticJobExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单作业执行器.
 * 
 * @author zhangliang
 */
@Slf4j
public final class SimpleElasticJobExecutor extends AbstractElasticJobExecutor {
    
    private final SimpleElasticJob simpleElasticJob;
    
    public SimpleElasticJobExecutor(final SimpleElasticJob simpleElasticJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.simpleElasticJob = simpleElasticJob;
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        simpleElasticJob.execute(shardingContext);
    }
}
