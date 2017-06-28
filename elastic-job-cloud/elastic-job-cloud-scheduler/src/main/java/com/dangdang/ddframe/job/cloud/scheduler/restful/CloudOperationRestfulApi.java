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

package com.dangdang.ddframe.job.cloud.scheduler.restful;

import com.dangdang.ddframe.job.cloud.scheduler.mesos.MesosStateService;
import com.dangdang.ddframe.job.cloud.scheduler.mesos.ReconcileService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * 作业云维护服务.
 * 
 * @author gaohongtao.
 */
@Path("/operate")
@Slf4j
public final class CloudOperationRestfulApi {
    
    private static ReconcileService reconcileService;
    
    private static final long RECONCILE_MILLIS_INTERVAL = 10 * 1000L;
    
    private static MesosStateService mesosStateService;
    
    private static long lastReconcileTime;
    
    /**
     * 初始化.
     * 
     * @param regCenter 注册中心
     * @param reconcileService 协调服务
     */
    public static void init(final CoordinatorRegistryCenter regCenter, final ReconcileService reconcileService) {
        CloudOperationRestfulApi.reconcileService = reconcileService;
        CloudOperationRestfulApi.mesosStateService = new MesosStateService(regCenter);
    }
    
    /**
     * 显示协调服务.
     * 
     */
    @POST
    @Path("/reconcile/explicit")
    public void explicitReconcile() {
        validReconcileInterval();
        reconcileService.explicitReconcile();
    }
    
    /**
     * 隐式协调服务.
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
     * 获取作业云App的沙箱信息.
     *
     * @param appName 云作业App配置名称
     * @return 沙箱信息
     * @throws JSONException JSON解析异常
     */
    @GET
    @Path("/sandbox")
    public JsonArray sandbox(@QueryParam("appName") final String appName) throws JSONException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "Lack param 'appName'");
        return mesosStateService.sandbox(appName);
    }
}
