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
 */

package com.dangdang.ddframe.job.cloud.api;

import com.dangdang.ddframe.job.cloud.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.cloud.exception.JobException;

/**
 * 弹性化分布式作业接口.
 * 
 * @author zhangliang
 */
public interface ElasticJob {
    
    /**
     * 执行作业.
     */
    void execute();
    
    /**
     * 处理作业执行时异常.
     * 
     * @param jobException 作业异常
     */
    void handleJobExecutionException(JobException jobException);
    
    /**
     * 获取提供作业服务的门面类.
     */
    JobFacade getJobFacade();
    
    /**
     * 设置提供作业服务的门面类.
     * 
     * @param jobFacade 提供作业服务的门面类
     */
    void setJobFacade(JobFacade jobFacade);
}
