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

import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.spring.namespace.parser.common.AbstractJobConfigurationDto;
import com.dangdang.ddframe.job.lite.spring.util.AopTargetUtils;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Properties;

/**
 * 基于Spring的作业启动器.
 *
 * @author caohao
 */
public class SpringJobScheduler extends JobScheduler implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    public SpringJobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final AbstractJobConfigurationDto jobConfigurationDto) {
        super(coordinatorRegistryCenter, jobConfigurationDto.toJobConfiguration());
    }
    
    public SpringJobScheduler(final CoordinatorRegistryCenter coordinatorRegistryCenter, final AbstractJobConfigurationDto jobConfigurationDto, final ElasticJobListener[] elasticJobListeners) {
        super(coordinatorRegistryCenter, jobConfigurationDto.toJobConfiguration(), getTargetElasticJobListeners(elasticJobListeners));
    }
    
    private static ElasticJobListener[] getTargetElasticJobListeners(final ElasticJobListener[] elasticJobListeners) {
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
