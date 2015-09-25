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

package com.dangdang.ddframe.job.spring.schedule;

import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import com.dangdang.ddframe.job.spring.util.AopTargetUtils;
import com.google.common.base.Preconditions;

/**
 * 基于Spring Bean的作业工厂.
 * 
 * @author zhangliang
 */
@Slf4j
public final class SpringJobFactory extends PropertySettingJobFactory {
    
    private static ApplicationContext applicationContext;
    
    public static void setApplicationContext(final ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
        Preconditions.checkNotNull(applicationContext, "applicationContext cannot be null, should call setApplicationContext first.");
        Job job = null;
        try {
            for (Job each : applicationContext.getBeansOfType(Job.class).values()) {
                if (AopUtils.getTargetClass(each) == bundle.getJobDetail().getJobClass()) {
                    job = each;
                    break;
                }
            }
            if (null == job) {
                throw new NoSuchBeanDefinitionException("");
            }
        } catch (final BeansException ex) {
            log.info("Elastic job: cannot found bean for class: '{}'. This job is not managed for spring.", bundle.getJobDetail().getJobClass().getCanonicalName());
            return super.newJob(bundle, scheduler);
        }
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(scheduler.getContext());
        jobDataMap.putAll(bundle.getJobDetail().getJobDataMap());
        jobDataMap.putAll(bundle.getTrigger().getJobDataMap());
        Job target = (Job) AopTargetUtils.getTarget(job);
        setBeanProps(target, jobDataMap);
        return target;
    }
}
