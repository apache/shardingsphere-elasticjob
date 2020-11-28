/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.error.handler;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.context.Reloadable;
import org.apache.shardingsphere.elasticjob.infra.context.ReloadablePostProcessor;
import org.apache.shardingsphere.elasticjob.infra.exception.JobConfigurationException;

import java.util.Optional;
import java.util.Properties;

/**
 * JobErrorHandler reloadable.
 */
@Slf4j
public final class JobErrorHandlerReloadable implements Reloadable<JobErrorHandler>, ReloadablePostProcessor {
    
    private String jobErrorHandlerType;
    
    private Properties props;
    
    private JobErrorHandler jobErrorHandler;
    
    @Override
    public void init(final JobConfiguration jobConfig) {
        jobErrorHandlerType = Strings.isNullOrEmpty(jobConfig.getJobErrorHandlerType()) ? JobErrorHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobErrorHandlerType();
        props = (Properties) jobConfig.getProps().clone();
        jobErrorHandler = JobErrorHandlerFactory.createHandler(jobErrorHandlerType, props)
                .orElseThrow(() -> new JobConfigurationException("Cannot find job error handler type '%s'.", jobErrorHandlerType));
    }
    
    @Override
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        String newJobErrorHandlerType = Strings.isNullOrEmpty(jobConfig.getJobErrorHandlerType()) ? JobErrorHandlerFactory.DEFAULT_HANDLER : jobConfig.getJobErrorHandlerType();
        if (newJobErrorHandlerType.equals(jobErrorHandlerType) && props.equals(jobConfig.getProps())) {
            return;
        }
        log.debug("JobErrorHandler reload occurred in the job '{}'. Change from '{}' to '{}'.", jobConfig.getJobName(), jobErrorHandlerType, newJobErrorHandlerType);
        reload(newJobErrorHandlerType, jobConfig.getProps());
    }
    
    private void reload(final String jobErrorHandlerType, final Properties props) {
        jobErrorHandler.close();
        this.jobErrorHandlerType = jobErrorHandlerType;
        this.props = (Properties) props.clone();
        jobErrorHandler = JobErrorHandlerFactory.createHandler(jobErrorHandlerType, props)
                .orElseThrow(() -> new JobConfigurationException("Cannot find job error handler type '%s'.", jobErrorHandlerType));
    }
    
    @Override
    public JobErrorHandler getInstance() {
        return jobErrorHandler;
    }
    
    @Override
    public String getType() {
        return JobErrorHandler.class.getName();
    }
    
    @Override
    public void close() {
        Optional.ofNullable(jobErrorHandler).ifPresent(JobErrorHandler::close);
    }
}
