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

package org.apache.shardingsphere.elasticjob.lite.console.controller;

import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobExecutionEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.request.FindJobStatusTraceEventsRequest;
import org.apache.shardingsphere.elasticjob.lite.console.dto.response.BasePageResponse;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceHistoryService;
import org.apache.shardingsphere.elasticjob.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.tracing.event.JobStatusTraceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * Event trace history RESTful API.
 */
@RestController
@RequestMapping("/event-trace")
public final class EventTraceHistoryController {
    
    @Autowired
    private EventTraceHistoryService eventTraceHistoryService;
    
    /**
     * Find job execution events.
     *
     * @param requestParams query criteria
     * @return job execution event trace result
     */
    @GetMapping(value = "/execution", produces = MediaType.APPLICATION_JSON)
    public BasePageResponse<JobExecutionEvent> findJobExecutionEvents(final FindJobExecutionEventsRequest requestParams) {
        Page<JobExecutionEvent> jobExecutionEvents = eventTraceHistoryService.findJobExecutionEvents(requestParams);
        return BasePageResponse.of(jobExecutionEvents);
    }
    
    /**
     * Find job status trace events.
     *
     * @param requestParams query criteria
     * @return job status trace result
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public BasePageResponse<JobStatusTraceEvent> findJobStatusTraceEvents(final FindJobStatusTraceEventsRequest requestParams) {
        Page<JobStatusTraceEvent> jobStatusTraceEvents = eventTraceHistoryService.findJobStatusTraceEvents(requestParams);
        return BasePageResponse.of(jobStatusTraceEvents);
    }
}
