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

import com.google.common.base.Strings;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Condition;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Result;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceDataSourceConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionEventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobExecutionEvent;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.JobStatusTraceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Event trace history RESTful API.
 */
@RestController
@RequestMapping("/event-trace")
public final class EventTraceHistoryController {
    
    private EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration = SessionEventTraceDataSourceConfiguration.getEventTraceDataSourceConfiguration();
    
    private EventTraceDataSourceConfigurationService eventTraceDataSourceConfigurationService;
    
    @Autowired
    public EventTraceHistoryController(final EventTraceDataSourceConfigurationService eventTraceDataSourceConfigurationService) {
        this.eventTraceDataSourceConfigurationService = eventTraceDataSourceConfigurationService;
    }
    
    /**
     * Find job execution events.
     *
     * @param requestParams query criteria
     * @return job execution event trace result
     * @throws ParseException parse exception
     */
    @GetMapping(value = "/execution", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public Result<JobExecutionEvent> findJobExecutionEvents(@RequestParam final MultiValueMap<String, String> requestParams) throws ParseException {
        if (!eventTraceDataSourceConfigurationService.loadActivated().isPresent()) {
            return new Result<>(0, new ArrayList<JobExecutionEvent>());
        }
        return new RDBJobEventSearch(setUpEventTraceDataSource()).findJobExecutionEvents(buildCondition(requestParams, new String[]{"jobName", "ip", "isSuccess"}));
    }
    
    /**
     * Find job status trace events.
     *
     * @param requestParams query criteria
     * @return job status trace result
     * @throws ParseException parse exception
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public Result<JobStatusTraceEvent> findJobStatusTraceEvents(@RequestParam final MultiValueMap<String, String> requestParams) throws ParseException {
        if (!eventTraceDataSourceConfigurationService.loadActivated().isPresent()) {
            return new Result<>(0, new ArrayList<JobStatusTraceEvent>());
        }
        return new RDBJobEventSearch(setUpEventTraceDataSource()).findJobStatusTraceEvents(buildCondition(requestParams, new String[]{"jobName", "source", "executionType", "state"}));
    }
    
    private DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(eventTraceDataSourceConfiguration.getDriver());
        result.setUrl(eventTraceDataSourceConfiguration.getUrl());
        result.setUsername(eventTraceDataSourceConfiguration.getUsername());
        result.setPassword(eventTraceDataSourceConfiguration.getPassword());
        return result;
    }
    
    private Condition buildCondition(final MultiValueMap<String, String> requestParams, final String[] params) throws ParseException {
        int perPage = 10;
        int page = 1;
        if (!Strings.isNullOrEmpty(requestParams.getFirst("per_page"))) {
            perPage = Integer.parseInt(requestParams.getFirst("per_page"));
        }
        if (!Strings.isNullOrEmpty(requestParams.getFirst("page"))) {
            page = Integer.parseInt(requestParams.getFirst("page"));
        }
        String sort = requestParams.getFirst("sort");
        String order = requestParams.getFirst("order");
        Date startTime = null;
        Date endTime = null;
        Map<String, Object> fields = getQueryParameters(requestParams, params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(requestParams.getFirst("startTime"))) {
            startTime = simpleDateFormat.parse(requestParams.getFirst("startTime"));
        }
        if (!Strings.isNullOrEmpty(requestParams.getFirst("endTime"))) {
            endTime = simpleDateFormat.parse(requestParams.getFirst("endTime"));
        }
        return new Condition(perPage, page, sort, order, startTime, endTime, fields);
    }
    
    private Map<String, Object> getQueryParameters(final MultiValueMap<String, String> requestParams, final String[] params) {
        final Map<String, Object> result = new HashMap<>();
        for (String each : params) {
            if (!Strings.isNullOrEmpty(requestParams.getFirst(each))) {
                result.put(each, requestParams.getFirst(each));
            }
        }
        return result;
    }
}
