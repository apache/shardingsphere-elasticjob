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

package io.elasticjob.lite.event.rdb;

import io.elasticjob.lite.event.JobEventListener;
import io.elasticjob.lite.event.type.JobExecutionEvent;
import io.elasticjob.lite.event.type.JobStatusTraceEvent;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 运行痕迹事件数据库监听器.
 *
 * @author caohao
 */
public final class JobEventRdbListener extends JobEventRdbIdentity implements JobEventListener {
    
    private final JobEventRdbStorage repository;
    
    public JobEventRdbListener(final DataSource dataSource) throws SQLException {
        repository = new JobEventRdbStorage(dataSource);
    }
    
    @Override
    public void listen(final JobExecutionEvent executionEvent) {
        repository.addJobExecutionEvent(executionEvent);
    }
    
    @Override
    public void listen(final JobStatusTraceEvent jobStatusTraceEvent) {
        repository.addJobStatusTraceEvent(jobStatusTraceEvent);
    }
}
