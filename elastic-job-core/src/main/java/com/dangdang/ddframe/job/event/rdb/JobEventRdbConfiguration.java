/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.event.rdb;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.JobTraceEvent.LogLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * 作业数据库事件配置.
 *
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class JobEventRdbConfiguration extends JobEventRdbIdentity implements JobEventConfiguration {
    
    private final String driverClassName;
    
    private final String url;
    
    private final String username;
    
    private final String password;
    
    private final LogLevel logLevel;
    
    @Override
    public JobEventListener createJobEventListener() {
        try {
            return new JobEventRdbListener(this);
        } catch (final SQLException ex) {
            log.error("Elastic job: create JobEventRdbListener failure, error is: ", ex);
        }
        return null;
    }
}
