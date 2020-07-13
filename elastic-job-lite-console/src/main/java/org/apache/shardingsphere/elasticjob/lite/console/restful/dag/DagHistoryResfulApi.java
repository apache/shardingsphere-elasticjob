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

package org.apache.shardingsphere.elasticjob.lite.console.restful.dag;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Result;
import org.apache.shardingsphere.elasticjob.lite.console.domain.EventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.console.service.EventTraceDataSourceConfigurationService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.EventTraceDataSourceConfigurationServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.console.util.SessionEventTraceDataSourceConfiguration;
import org.apache.shardingsphere.elasticjob.lite.tracing.event.DagJobExecutionEvent;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Dag history trace.
 *
 * @author: dutengxiao
 **/
@Slf4j
@Path("/dag/history")
public final class DagHistoryResfulApi {
    private EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration = SessionEventTraceDataSourceConfiguration.getEventTraceDataSourceConfiguration();

    private EventTraceDataSourceConfigurationService eventTraceDataSourceConfigurationService = new EventTraceDataSourceConfigurationServiceImpl();

    /**
     * Query dag history trace info.
     *
     * @param uriInfo query info
     * @return dag graph
     * @throws ParseException parse exception
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<DagJobExecutionEvent> load(@Context final UriInfo uriInfo) throws ParseException {
        if (!eventTraceDataSourceConfigurationService.loadActivated().isPresent()) {
            return new RDBJobEventSearch.Result<>(0, new ArrayList<DagJobExecutionEvent>());
        }
        return new RDBJobEventSearch(setUpEventTraceDataSource()).findDagJobExecutionEvents(buildCondition(uriInfo, new String[]{"groupName", "batchNo"}));
    }

    private DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(eventTraceDataSourceConfiguration.getDriver());
        result.setUrl(eventTraceDataSourceConfiguration.getUrl());
        result.setUsername(eventTraceDataSourceConfiguration.getUsername());
        result.setPassword(eventTraceDataSourceConfiguration.getPassword());
        return result;
    }

    private RDBJobEventSearch.Condition buildCondition(final UriInfo info, final String[] params) throws ParseException {
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
        return new RDBJobEventSearch.Condition(perPage, page, sort, order, startTime, endTime, fields);
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
