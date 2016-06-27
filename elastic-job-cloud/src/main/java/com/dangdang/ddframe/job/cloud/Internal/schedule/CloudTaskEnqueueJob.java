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

package com.dangdang.ddframe.job.cloud.Internal.schedule;

import com.dangdang.ddframe.job.cloud.Internal.queue.TaskQueueService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 向注册中心发布任务的作业.
 *
 * @author zhangliang
 */
public final class CloudTaskEnqueueJob implements Job {
    
    private String jobName;
    
    private TaskQueueService taskQueueService;
    
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        taskQueueService.enqueue(jobName);
    }
    
    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }
    
    public void setTaskQueueService(final TaskQueueService taskQueueService) {
        this.taskQueueService = taskQueueService;
    }
}
