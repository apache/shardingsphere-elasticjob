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

import lombok.Getter;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Closeable;
import java.util.Properties;

/**
 * Job error handler reloader.
 */
public final class JobErrorHandlerReloader implements Closeable {
    
    private Properties props;
    
    @Getter
    private JobErrorHandler jobErrorHandler;
    
    public JobErrorHandlerReloader(final JobConfiguration jobConfig) {
        props = (Properties) jobConfig.getProps().clone();
        jobErrorHandler = TypedSPILoader.getService(JobErrorHandler.class, jobConfig.getJobErrorHandlerType(), props);
    }
    
    /**
     * Reload if necessary.
     *
     * @param jobConfig job configuration
     */
    public synchronized void reloadIfNecessary(final JobConfiguration jobConfig) {
        if (jobErrorHandler.getType().equals(jobConfig.getJobErrorHandlerType()) && props.equals(jobConfig.getProps())) {
            return;
        }
        reload(jobConfig.getJobErrorHandlerType(), jobConfig.getProps());
    }
    
    private void reload(final String jobErrorHandlerType, final Properties props) {
        jobErrorHandler.close();
        this.props = (Properties) props.clone();
        jobErrorHandler = TypedSPILoader.getService(JobErrorHandler.class, jobErrorHandlerType, props);
    }
    
    @Override
    public void close() {
        jobErrorHandler.close();
    }
}
