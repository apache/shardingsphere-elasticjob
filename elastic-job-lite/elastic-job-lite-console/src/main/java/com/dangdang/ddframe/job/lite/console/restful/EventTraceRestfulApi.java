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

package com.dangdang.ddframe.job.lite.console.restful;

import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Condition;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbSearch.Result;
import com.dangdang.ddframe.job.event.type.JobExecutionEvent;
import com.dangdang.ddframe.job.event.type.JobStatusTraceEvent;
import com.dangdang.ddframe.job.lite.console.domain.DataSourceConfiguration;
import com.dangdang.ddframe.job.lite.console.util.SessionDataSourceConfiguration;
import com.google.common.base.Strings;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("/event-trace")
public class EventTraceRestfulApi {
    
    DataSourceConfiguration dataSourceConfiguration = SessionDataSourceConfiguration.getDataSourceConfiguration();
    
    @GET
    @Path("/execution")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<JobExecutionEvent> findJobExecutionEvents(@Context final UriInfo info) throws ParseException {
        JobEventRdbSearch jobEventRdbSearch = new JobEventRdbSearch(setUpEventTraceDataSource());
        return jobEventRdbSearch.findJobExecutionEvents(buildCondition(info, new String[]{"jobName", "ip", "isSuccess"}));
    }
    
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<JobStatusTraceEvent> findJobStatusTraceEvents(@Context final UriInfo info) throws ParseException {
        JobEventRdbSearch jobEventRdbSearch = new JobEventRdbSearch(setUpEventTraceDataSource());
        return jobEventRdbSearch.findJobStatusTraceEvents(buildCondition(info, new String[]{"jobName", "source", "executionType", "state"}));
    }
    
    private DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(dataSourceConfiguration.getDriver());
        result.setUrl(dataSourceConfiguration.getUrl());
        result.setUsername(dataSourceConfiguration.getUsername());
        result.setPassword(dataSourceConfiguration.getPassword());
        return result;
    }
    
    private Condition buildCondition(final UriInfo info, final String[] params) throws ParseException {
        int perPage = 10;
        int page = 1;
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("per_page"))) {
            perPage = Integer.parseInt(info.getQueryParameters().getFirst("per_page"));
        }
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("page"))) {
            page = Integer.parseInt(info.getQueryParameters().getFirst("page"));
        }
        String sort = info.getQueryParameters().getFirst("sort");
        String order = info.getQueryParameters().getFirst("order");
        Date startTime = null;
        Date endTime = null;
        Map<String, Object> fields = getQueryParameters(info, params);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("startTime"))) {
            startTime = simpleDateFormat.parse(info.getQueryParameters().getFirst("startTime"));
        }
        if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst("endTime"))) {
            endTime = simpleDateFormat.parse(info.getQueryParameters().getFirst("endTime"));
        }
        return new Condition(perPage, page, sort, order, startTime, endTime, fields);
    }
    
    private Map<String, Object> getQueryParameters(final UriInfo info, final String[] params) {
        final Map<String, Object> result = new HashMap<>();
        for (String each : params) {
            if (!Strings.isNullOrEmpty(info.getQueryParameters().getFirst(each))) {
                result.put(each, info.getQueryParameters().getFirst(each));
            }
        }
        return result;
    }
}
