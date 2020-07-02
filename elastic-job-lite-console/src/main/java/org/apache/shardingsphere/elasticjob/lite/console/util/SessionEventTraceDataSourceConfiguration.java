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

package org.apache.shardingsphere.elasticjob.lite.console.util;

import org.apache.shardingsphere.elasticjob.lite.console.config.DynamicDataSourceConfig.DynamicDataSourceContextHolder;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Event trace data source configuration in session.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionEventTraceDataSourceConfiguration {
    
    private static EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration;
    
    /**
     * Set event trace data source configuration.
     *
     * @param eventTraceDataSourceConfiguration event trace data source configuration
     */
    public static void setDataSourceConfiguration(final EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration) {
        DynamicDataSourceContextHolder.setDataSourceName(eventTraceDataSourceConfiguration.getName());
        SessionEventTraceDataSourceConfiguration.eventTraceDataSourceConfiguration = eventTraceDataSourceConfiguration;
    }
}
