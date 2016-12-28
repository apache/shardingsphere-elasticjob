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

package com.dangdang.ddframe.job.cloud.scheduler.statistics.job;

import java.util.HashMap;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.dangdang.ddframe.job.statistics.StatisticInterval;

public class TestStatisticJob extends AbstractStatisticJob {
    
    @Override
    public JobDetail buildJobDetail() {
        return JobBuilder.newJob(this.getClass()).withIdentity(getJobName()).build();
    }
    
    @Override
    public Trigger buildTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity(getTriggerName())
                .withSchedule(CronScheduleBuilder.cronSchedule(StatisticInterval.MINUTE.getCron())
                .withMisfireHandlingInstructionDoNothing()).build();
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> result = new HashMap<>(2);
        result.put("key", "value");
        return result;
    }
    
    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        System.out.println("do something...");
    }
}
