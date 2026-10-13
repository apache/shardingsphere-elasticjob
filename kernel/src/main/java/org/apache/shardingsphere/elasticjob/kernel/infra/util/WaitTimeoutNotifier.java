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

package org.apache.shardingsphere.elasticjob.kernel.infra.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.spi.executor.error.handler.JobErrorHandler;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Notifies the job's configured error handler when a bounded coordination wait loop
 * (sharding, leader election) gives up instead of blocking indefinitely.
 */
@Slf4j
public final class WaitTimeoutNotifier {
    
    private WaitTimeoutNotifier() {
    }
    
    /**
     * Notify of a bounded-wait timeout.
     *
     * @param jobName job name
     * @param jobConfig job configuration
     * @param waitDescription description of what was being waited for, for logging
     */
    public static void notifyTimeout(final String jobName, final JobConfiguration jobConfig, final String waitDescription) {
        String message = String.format("Job '%s' waited more than %d ms for %s; giving up to avoid indefinite blocking.",
                jobName, jobConfig.getMaxWaitMillis(), waitDescription);
        log.error(message);
        if (null == jobConfig.getJobErrorHandlerType()) {
            return;
        }
        JobErrorHandler jobErrorHandler = TypedSPILoader.getService(JobErrorHandler.class, jobConfig.getJobErrorHandlerType(), jobConfig.getProps());
        try {
            jobErrorHandler.handleException(jobName, new IllegalStateException(message));
        } finally {
            jobErrorHandler.close();
        }
    }
}
