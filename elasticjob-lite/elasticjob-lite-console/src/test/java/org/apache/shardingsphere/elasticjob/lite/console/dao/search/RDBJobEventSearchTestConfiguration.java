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

package org.apache.shardingsphere.elasticjob.lite.console.dao.search;

import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobExecutionEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobStatusTraceEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceHistoryService;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.apache.shardingsphere.elasticjob.tracing.rdb.storage.RDBJobEventStorage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

@TestConfiguration
public class RDBJobEventSearchTestConfiguration implements InitializingBean {
    
    @Autowired
    private EventTraceHistoryService eventTraceHistoryService;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        initStorage();
    }
    
    private void initStorage() throws SQLException {
        eventTraceHistoryService.findJobExecutionEvents(new FindJobExecutionEventsRequest(10, 1));
        eventTraceHistoryService.findJobStatusTraceEvents(new FindJobStatusTraceEventsRequest(10, 1));
        RDBJobEventStorage storage = new RDBJobEventStorage(dataSource);
        for (int i = 1; i <= 500L; i++) {
            JobExecutionEvent startEvent = new JobExecutionEvent("localhost", "127.0.0.1", "fake_task_id", "test_job_" + i, JobExecutionEvent.ExecutionSource.NORMAL_TRIGGER, 0);
            storage.addJobExecutionEvent(startEvent);
            if (i % 2 == 0) {
                JobExecutionEvent successEvent = startEvent.executionSuccess();
                storage.addJobExecutionEvent(successEvent);
            }
            storage.addJobStatusTraceEvent(new JobStatusTraceEvent(
                    "test_job_" + i,
                    "fake_failed_failover_task_id",
                    "fake_slave_id",
                    JobStatusTraceEvent.Source.LITE_EXECUTOR,
                    "FAILOVER",
                    "0",
                    JobStatusTraceEvent.State.TASK_FAILED,
                    "message is empty."
            ));
        }
    }
}
