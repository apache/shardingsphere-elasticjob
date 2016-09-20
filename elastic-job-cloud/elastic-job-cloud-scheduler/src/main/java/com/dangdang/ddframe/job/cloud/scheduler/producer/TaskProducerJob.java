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

package com.dangdang.ddframe.job.cloud.scheduler.producer;

import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.dangdang.ddframe.job.cloud.scheduler.state.ready.ReadyService;

import java.util.List;

/**
 * 发布任务的作业.
 *
 * @author caohao
 */
@Setter
public final class TaskProducerJob implements Job {
    
    private ReadyService readyService;
    
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        List<String> jobNames = TaskProducerJobContext.getInstance().get(context.getJobDetail().getKey());
        if (jobNames != null) {
            for (String each : jobNames) {
                readyService.addTransient(each);
            }
        }
    }
}
