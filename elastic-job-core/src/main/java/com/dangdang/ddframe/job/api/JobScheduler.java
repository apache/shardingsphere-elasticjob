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

package com.dangdang.ddframe.job.api;

import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 作业调度器.
 * 
 * @author zhangliang
 * @author caohao
 */
@Slf4j
public class JobScheduler {
    
    private static final String SCHEDULER_INSTANCE_NAME_SUFFIX = "Scheduler";
    
    private static final String CRON_TRIGGER_IDENTITY_SUFFIX = "Trigger";
    
    private final String jobName;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final SchedulerFacade schedulerFacade;
    
    private final JobFacade jobFacade;
    
    private final JobDetail jobDetail;
    
    public JobScheduler(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        jobConfig.init();
        jobName = jobConfig.getJobName();
        this.regCenter = regCenter;
        List<ElasticJobListener> elasticJobListenerList = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobConfig, elasticJobListenerList);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig, elasticJobListenerList);
        jobFacade = new JobFacade(regCenter, jobConfig, elasticJobListenerList);
        jobDetail = JobBuilder.newJob(jobConfig.getJobClass()).withIdentity(jobName).build();
    }

    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig);
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    /**
     * 初始化作业.
     */
    public void init() {
        log.debug("Elastic job: job controller init, job name is: {}.", jobName);
        schedulerFacade.clearPreviousServerStatus();
        regCenter.addCacheData("/" + jobName);
        schedulerFacade.registerStartUpInfo();
        jobDetail.getJobDataMap().put("jobFacade", jobFacade);
        JobScheduleController jobScheduleController;
        try {
            jobScheduleController = new JobScheduleController(
                    initializeScheduler(jobDetail.getKey().toString()), jobDetail, schedulerFacade, Joiner.on("_").join(jobName, CRON_TRIGGER_IDENTITY_SUFFIX));
            jobScheduleController.scheduleJob(schedulerFacade.getCron());
        } catch (final SchedulerException ex) {
            throw new JobException(ex);
        }
        JobRegistry.getInstance().addJobScheduleController(jobName, jobScheduleController);
    }
    
    private Scheduler initializeScheduler(final String jobName) throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(getBaseQuartzProperties(jobName));
        Scheduler result = factory.getScheduler();
        result.getListenerManager().addTriggerListener(schedulerFacade.newJobTriggerListener());
        return result;
    }
    
    private Properties getBaseQuartzProperties(final String jobName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", Joiner.on("_").join(jobName, SCHEDULER_INSTANCE_NAME_SUFFIX));
        if (!schedulerFacade.isMisfire()) {
            result.put("org.quartz.jobStore.misfireThreshold", "1");
        }
        prepareEnvironments(result);
        return result;
    }
    
    protected void prepareEnvironments(final Properties props) {
    }
}
