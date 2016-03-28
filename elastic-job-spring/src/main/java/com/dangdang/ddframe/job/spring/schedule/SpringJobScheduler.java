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

package com.dangdang.ddframe.job.spring.schedule;

import java.util.Properties;

import com.dangdang.ddframe.job.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.spring.util.AopTargetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.api.JobScheduler;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 基于Spring的作业启动器.
 *
 * @author caohao
 */
public class SpringJobScheduler extends JobScheduler implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    public SpringJobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        super(coordinatorRegistryCenter, jobConfiguration);
    }
    
    public SpringJobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration, final ElasticJobListener[] elasticJobListeners) {
        super(coordinatorRegistryCenter, jobConfiguration, getTargetElasticJobListeners(elasticJobListeners));
    }
    
    private static ElasticJobListener[] getTargetElasticJobListeners(ElasticJobListener[] elasticJobListeners) {
        final ElasticJobListener[] result = new ElasticJobListener[elasticJobListeners.length];
        for (int i = 0; i < elasticJobListeners.length; i++) {
            result[i] = (ElasticJobListener) AopTargetUtils.getTarget(elasticJobListeners[i]);
        }
        return result;
    }
    
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    protected void prepareEnvironments(final Properties props) {
        SpringJobFactory.setApplicationContext(applicationContext);
        props.put("org.quartz.scheduler.jobFactory.class", SpringJobFactory.class.getName());
    }
}
