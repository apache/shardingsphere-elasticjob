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

import com.dangdang.ddframe.job.cloud.scheduler.producer.ProducerManager;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

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
    
    private static ProducerManager producerManager;
    
    private static final long RECONCILE_INTERVAL = 10 * 1000;
    
    private static long lastReconcileTime;
    
    /**
     * 初始化.
     * 
     * @param producerManager 生产管理器
     */
    public static void init(final ProducerManager producerManager) {
        CloudOperationRestfulApi.producerManager = producerManager;
    }
    
    /**
     * 显示协调服务.
     * 
     * @param taskId 可选参数,如果存在taskId那么只针对该task进行协调.如果不传入taskId,对所有的服务进行协调
     */
    @POST
    @Path("/reconcile/explicit")
    public void explicitReconcile(@QueryParam("taskId") final String taskId) {
        synchronized (CloudOperationRestfulApi.class) {
            validReconcileInterval();
            if (Strings.isNullOrEmpty(taskId)) {
                producerManager.explicitReconcile();
            } else {
                producerManager.explicitReconcile(taskId);
            }
        }
    }
    
    /**
     * 隐式协调服务.
     */
    @POST
    @Path("/reconcile/implicit")
    public void implicitReconcile() {
        synchronized (CloudOperationRestfulApi.class) {
            validReconcileInterval();
            producerManager.implicitReconcile();
        }
    }
    
    private void validReconcileInterval() {
        if (System.currentTimeMillis() < lastReconcileTime + RECONCILE_INTERVAL) {
            throw new RuntimeException("Repeat explicitReconcile");
        }
        lastReconcileTime = System.currentTimeMillis();
    }
}
