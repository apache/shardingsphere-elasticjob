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

package com.dangdang.ddframe.job.internal.executor;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.api.listener.AbstractDistributeOnceElasticJobListener;
import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.exception.JobException;
import com.dangdang.ddframe.job.internal.guarantee.GuaranteeService;
import com.dangdang.ddframe.job.internal.schedule.JobFacade;
import com.dangdang.ddframe.job.internal.schedule.SchedulerFacade;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 作业启动器.
 * 
 * @author zhangliang
 */
@Slf4j
@Getter
public class JobExecutor {
    
    private final String jobName;
    
    private final CoordinatorRegistryCenter regCenter;
    
    private final ElasticJob elasticJob;
    
    private final SchedulerFacade schedulerFacade;
    
    public JobExecutor(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final ElasticJobListener... elasticJobListeners) {
        jobName = jobConfig.getJobName();
        this.regCenter = regCenter;
        List<ElasticJobListener> elasticJobListenerList = Arrays.asList(elasticJobListeners);
        setGuaranteeServiceForElasticJobListeners(regCenter, jobConfig, elasticJobListenerList);
        elasticJob = createElasticJob(jobConfig, elasticJobListenerList);
        schedulerFacade = new SchedulerFacade(regCenter, jobConfig, elasticJobListenerList);
    }
    
    private void setGuaranteeServiceForElasticJobListeners(final CoordinatorRegistryCenter regCenter, final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListeners) {
        GuaranteeService guaranteeService = new GuaranteeService(regCenter, jobConfig);
        for (ElasticJobListener each : elasticJobListeners) {
            if (each instanceof AbstractDistributeOnceElasticJobListener) {
                ((AbstractDistributeOnceElasticJobListener) each).setGuaranteeService(guaranteeService);
            }
        }
    }
    
    private ElasticJob createElasticJob(final JobConfiguration jobConfig, final List<ElasticJobListener> elasticJobListenerList) {
        ElasticJob result;
        try {
            result = (ElasticJob) jobConfig.getJobClass().newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new JobException(ex);
        }
        result.setJobFacade(new JobFacade(regCenter, jobConfig, elasticJobListenerList));
        return result;
    }
    
    /**
     * 初始化作业.
     */
    public void init() {
        log.debug("Elastic job: job controller init, job name is: {}.", jobName);
        schedulerFacade.clearPreviousServerStatus();
        regCenter.addCacheData("/" + jobName);
        schedulerFacade.registerStartUpInfo();
    }
}
