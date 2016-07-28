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

package com.dangdang.ddframe.job.lite.api;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.internal.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.executor.DataflowJobExecutor;
import com.dangdang.ddframe.job.api.type.script.executor.ScriptJobExecutor;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJob;
import com.dangdang.ddframe.job.api.type.simple.executor.SimpleJobExecutor;
import com.dangdang.ddframe.job.exception.JobException;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Elastic Job Lite提供的Quartz封装作业.
 *
 * @author zhangliang
 */
public class LiteJob implements Job {
    
    @Setter
    private ElasticJob elasticJob;
    
    @Setter
    private JobFacade jobFacade;
    
    @SuppressWarnings("unchecked")
    @Override
    // TODO 和cloud一起提炼
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        AbstractElasticJobExecutor elasticJobExecutor;
        if (null == elasticJob) {
            elasticJobExecutor = new ScriptJobExecutor(jobFacade);
        } else if (elasticJob instanceof SimpleJob) {
            elasticJobExecutor = new SimpleJobExecutor((SimpleJob) elasticJob, jobFacade);
        } else if (elasticJob instanceof DataflowJob) {
            elasticJobExecutor = new DataflowJobExecutor((DataflowJob<Object>) elasticJob, jobFacade);
        } else {
            throw new JobException("Cannot support job type '%s'", elasticJob.getClass());
        }
        elasticJobExecutor.execute();
    }
}
