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

package org.apache.shardingsphere.elasticjob.lite.tracing.rdb.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.elasticjob.lite.tracing.config.JobEventConfiguration;
import org.apache.shardingsphere.elasticjob.lite.tracing.exception.TracingConfigurationException;
import org.apache.shardingsphere.elasticjob.lite.tracing.listener.JobEventListener;
import org.apache.shardingsphere.elasticjob.lite.tracing.rdb.listener.JobEventRdbListener;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Job event RDB configuration.
 */
@RequiredArgsConstructor
@Getter
public final class JobEventRdbConfiguration implements JobEventConfiguration, Serializable {
    
    private static final long serialVersionUID = 3344410699286435226L;
    
    private final transient DataSource dataSource;
    
    @Override
    public JobEventListener createJobEventListener() throws TracingConfigurationException {
        try {
            return new JobEventRdbListener(dataSource);
        } catch (final SQLException ex) {
            throw new TracingConfigurationException(ex);
        }
    }
}
