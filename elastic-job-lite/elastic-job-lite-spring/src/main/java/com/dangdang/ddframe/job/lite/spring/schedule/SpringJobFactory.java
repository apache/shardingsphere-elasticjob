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

package com.dangdang.ddframe.job.lite.spring.schedule;

import com.dangdang.ddframe.job.lite.api.ElasticJob;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * 基于Spring Bean的作业工厂.
 * 
 * @author zhangliang
 * @author caohao
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
        Job result = super.newJob(bundle, scheduler);
        Optional<ElasticJob> elasticJobBean = findElasticJobBean(bundle);
        if (elasticJobBean.isPresent()) {
            setBeanProps(result, getJobDataMap(bundle, scheduler, elasticJobBean.get()));
        }
        return result;
    }
    
    private Optional<ElasticJob> findElasticJobBean(final TriggerFiredBundle bundle) {
        for (ElasticJob each : applicationContext.getBeansOfType(ElasticJob.class).values()) {
            if (bundle.getJobDetail().getJobDataMap().containsKey("elasticJob") && AopUtils.getTargetClass(each) == bundle.getJobDetail().getJobDataMap().get("elasticJob").getClass()) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private JobDataMap getJobDataMap(final TriggerFiredBundle bundle, final Scheduler scheduler, final ElasticJob elasticJobBean) throws SchedulerException {
        JobDataMap result = new JobDataMap();
        result.putAll(scheduler.getContext());
        result.putAll(bundle.getJobDetail().getJobDataMap());
        result.putAll(bundle.getTrigger().getJobDataMap());
        elasticJobBean.setJobFacade(((ElasticJob) result.get("elasticJob")).getJobFacade());
        result.put("elasticJob", elasticJobBean);
        return result;
    }
}
