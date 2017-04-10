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

package com.dangdang.ddframe.job.cloud.scheduler.state.disable.app;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.extern.slf4j.Slf4j;

/**
 * 禁用应用队列服务.
 *
 * @author caohao
 */
@Slf4j
public class DisableAppService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final CoordinatorRegistryCenter regCenter;
    
    public DisableAppService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    /**
     * 将应用放入禁用队列.
     *
     * @param appName 应用名称
     */
    public void add(final String appName) {
        if (regCenter.getNumChildren(DisableAppNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add disable app, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        String disableAppNodePath = DisableAppNode.getDisableAppNodePath(appName);
        if (!regCenter.isExisted(disableAppNodePath)) {
            regCenter.persist(disableAppNodePath, appName);
        }
    }
    
    /**
     * 从禁用应用队列中删除应用.
     * 
     * @param appName 待删除的应用名称
     */
    public void remove(final String appName) {
        regCenter.remove(DisableAppNode.getDisableAppNodePath(appName));
    }
    
    /**
     * 判断应用是否在禁用应用队列中.
     * 
     * @param appName 应用名称
     * @return 应用是否被禁用
     */
    public boolean isDisabled(final String appName) {
        return regCenter.isExisted(DisableAppNode.getDisableAppNodePath(appName));
    }
}
