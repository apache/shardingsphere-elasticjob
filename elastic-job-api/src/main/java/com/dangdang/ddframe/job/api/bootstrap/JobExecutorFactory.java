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

package com.dangdang.ddframe.job.api.bootstrap;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.exception.JobConfigurationException;
import com.dangdang.ddframe.job.api.exception.JobSystemException;
import com.dangdang.ddframe.job.api.internal.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.api.internal.executor.JobFacade;
import com.dangdang.ddframe.job.api.type.dataflow.api.DataflowJob;
import com.dangdang.ddframe.job.api.type.dataflow.executor.DataflowJobExecutor;
import com.dangdang.ddframe.job.api.type.script.api.ScriptJob;
import com.dangdang.ddframe.job.api.type.script.executor.ScriptJobExecutor;
import com.dangdang.ddframe.job.api.type.simple.api.SimpleJob;
import com.dangdang.ddframe.job.api.type.simple.executor.SimpleJobExecutor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业执行器工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobExecutorFactory {
    
    /**
     * 获取作业执行器.
     *
     * @param elasticJobClass 作业类
     * @param jobFacade 作业内部服务门面服务
     * @return 作业执行器
     */
    @SuppressWarnings("unchecked")
    public static AbstractElasticJobExecutor getJobExecutor(final Class<? extends ElasticJob> elasticJobClass, final JobFacade jobFacade) {
        if (ScriptJob.class.isAssignableFrom(elasticJobClass)) {
            return new ScriptJobExecutor(jobFacade);
        }
        ElasticJob elasticJob;
        try {
            elasticJob = elasticJobClass.getConstructor().newInstance();
        }  catch (final ReflectiveOperationException ex) {
            throw new JobSystemException("Elastic job class must have a no argument constructor, class initialize failure, the error message is : '%s'", ex.getMessage());
        }
        if (elasticJob instanceof SimpleJob) {
            return new SimpleJobExecutor((SimpleJob) elasticJob, jobFacade);
        }
        if (elasticJob instanceof DataflowJob) {
            return new DataflowJobExecutor((DataflowJob) elasticJob, jobFacade);
        }
        throw new JobConfigurationException("Cannot support job type '%s'", elasticJobClass.getCanonicalName());
    }
}
