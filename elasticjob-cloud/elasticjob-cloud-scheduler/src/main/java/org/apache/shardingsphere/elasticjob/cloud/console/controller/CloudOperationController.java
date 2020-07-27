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

package org.apache.shardingsphere.elasticjob.cloud.console.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonParseException;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.MesosStateService;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.mesos.ReconcileService;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cloud operation restful api.
 */
@Slf4j
@RestController
@RequestMapping("/operate")
public final class CloudOperationController {
    
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
        CloudOperationController.reconcileService = reconcileService;
        CloudOperationController.mesosStateService = new MesosStateService(regCenter);
    }
    
    /**
     * Explicit reconcile service.
     */
    @PostMapping("/reconcile/explicit")
    public void explicitReconcile() {
        validReconcileInterval();
        reconcileService.explicitReconcile();
    }
    
    /**
     * Implicit reconcile service.
     */
    @PostMapping("/reconcile/implicit")
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
     * @throws JsonParseException parse json exception
     */
    @GetMapping("/sandbox")
    public Collection<Map<String, String>> sandbox(@RequestParam("appName") final String appName) throws JsonParseException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(appName), "Lack param 'appName'");
        return mesosStateService.sandbox(appName);
    }
}
