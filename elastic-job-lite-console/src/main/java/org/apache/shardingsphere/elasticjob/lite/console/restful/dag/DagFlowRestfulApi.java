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

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.lite.console.dao.search.RDBJobEventSearch.Result;
import org.apache.shardingsphere.elasticjob.lite.console.service.JobAPIService;
import org.apache.shardingsphere.elasticjob.lite.console.service.impl.JobAPIServiceImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.DagBriefInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.util.List;

/**
 * Dag configuration and operator restfulApi.
 *
 * @author: dutengxiao
 */
@Slf4j
@Path("/dag")
public final class DagFlowRestfulApi {

    private JobAPIService jobAPIService = new JobAPIServiceImpl();

    /**
     * get all dag flows.
     *
     * @param uriInfo query info
     * @return dagBriefInfo
     * @throws ParseException exception
     */
    @GET
    @Path("/flow")
    @Produces(MediaType.APPLICATION_JSON)
    public Result<DagBriefInfo> load(@Context final UriInfo uriInfo) throws ParseException {
        String flowCode = uriInfo.getQueryParameters().getFirst("flowCode");
        if (StringUtils.isEmpty(flowCode)) {
            List<DagBriefInfo> dagGroupNameList = jobAPIService.getDagOperateApi().getDagGroupNameList();
            Result<DagBriefInfo> result = new Result<>(dagGroupNameList.size(), dagGroupNameList);
            return result;
        }

        List<DagBriefInfo> dagJobDependencies = jobAPIService.getDagOperateApi().getDagJobDependencies(flowCode);
        if (dagJobDependencies.isEmpty()) {
            return new Result<>(0, null);
        }
        return new Result<>(1, Lists.newArrayList(new DagBriefInfo(flowCode)));
    }

    /**
     * Get dag details.
     *
     * @param flowCode dag flow code
     * @return DagBriefInfo
     */
    @GET
    @Path("/detail/{flowCode}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Result<DagBriefInfo> detail(final @PathParam("flowCode") String flowCode) {
        if (StringUtils.isEmpty(flowCode)) {
            return new Result<>(0, null);
        }
        List<DagBriefInfo> dagJobDependencies = jobAPIService.getDagOperateApi().getDagJobDependencies(flowCode);
        return new Result<>(dagJobDependencies.size(), dagJobDependencies);
    }

    /**
     * Do dag operate.
     *
     * @param flowCode dag flow code
     * @param operate do
     * @return Response
     */
    @POST
    @Path("/{flowCode}/{operate}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response operate(final @PathParam("flowCode") String flowCode, final @PathParam("operate") String operate) {
        log.info("Receive Dag-{} operate request, do - {}", flowCode, operate);
        boolean res = false;

        if (StringUtils.equals(operate, "start")) {
            res = jobAPIService.getDagOperateApi().toggleDagStart(flowCode);
        } else if (StringUtils.equals(operate, "stop")) {
            res = jobAPIService.getDagOperateApi().toggleDagStop(flowCode);
        } else if (StringUtils.equals(operate, "pause")) {
            res = jobAPIService.getDagOperateApi().toggleDagPause(flowCode);
        } else if (StringUtils.equals(operate, "resume")) {
            res = jobAPIService.getDagOperateApi().toggleDagResume(flowCode);
        } else if (StringUtils.equals(operate, "rerun")) {
            res = jobAPIService.getDagOperateApi().toggleDagRerunWhenFail(flowCode);
        } else {
            log.error("Not support operate-{}", operate);
            return new Response(false, "Not support operate");
        }
        return new Response(res, operate + ":" + (res ? "Success" : "Fail"));
    }

    @Data
    @AllArgsConstructor
    public static class Response {

        private boolean success;

        private String error;
    }
}
