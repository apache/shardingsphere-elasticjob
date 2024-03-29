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

package org.apache.shardingsphere.elasticjob.error.handler.normal;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.spi.executor.error.handler.JobErrorHandler;

/**
 * Job error handler for log error message.
 */
@Slf4j
public final class LogJobErrorHandler implements JobErrorHandler {
    
    @Override
    public void handleException(final String jobName, final Throwable cause) {
        log.error(String.format("Job '%s' exception occur in job processing", jobName), cause);
    }
    
    @Override
    public String getType() {
        return "LOG";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
