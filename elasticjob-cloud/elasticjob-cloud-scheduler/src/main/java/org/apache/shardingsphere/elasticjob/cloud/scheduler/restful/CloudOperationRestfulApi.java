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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.restful;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Cloud operation restful api.
 */
@Path("/operate")
@Slf4j
public final class CloudOperationRestfulApi {
    
    private static ReconcileService reconcileService;
    
    private static final long RECONCILE_MILLIS_INTERVAL = 10 * 1000L;
    
    private static MesosStateService mesosStateService;
    
    private static long lastReconcileTime;
    
    /**
     * Init.
     * 
     * @param regCenter registry center
     * @param reconcileService reconcile service
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ReconcileService reconcileService) {
        CloudOperationRestfulApi.reconcileService = reconcileService;
        CloudOperationRestfulApi.mesosStateService = new MesosStateService(regCenter);
    }
    
    /**
     * Explicit reconcile service.
     */
    @POST
    @Path("/reconcile/explicit")
    public void explicitReconcile() {
        validReconcileInterval();
        reconcileService.explicitReconcile();
    }
    
    /**
     * Implicit reconcile service.
     */
    @POST
    @Path("/reconcile/implicit")
    public void implicitReconcile() {
        validReconcileInterval();
        reconcileService.implicitReconcile();
    }
    
    private void validReconcileInterval() {
        if (System.currentTimeMillis() < lastReconcileTime + RECONCILE_MILLIS_INTERVAL) {
            throw new RuntimeException("Repeat explicitReconcile");
        }
        lastReconcileTime = System.currentTimeMillis();
    }
    
    /**
     * Get sandbox of the cloud job by app name.
     *
     * @param appName application name
     * @return sandbox info
     * @throws JSONException parse json exception
     */
    @GET
    @Path("/sandbox")
    public JsonArray sandbox(@QueryParam("appName") final String appName) throws JSONException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "Lack param 'appName'");
        return mesosStateService.sandbox(appName);
    }
}
