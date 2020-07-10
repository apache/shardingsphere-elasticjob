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

package org.apache.shardingsphere.elasticjob.tracing.rdb.listener;

import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.listener.TracingListener;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.RDBJobEventStorage;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * RDB tracing listener.
 */
public final class RDBTracingListener implements TracingListener {
    
    private final RDBJobEventStorage repository;
    
    public RDBTracingListener(final DataSource dataSource) throws SQLException {
        repository = new RDBJobEventStorage(dataSource);
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
