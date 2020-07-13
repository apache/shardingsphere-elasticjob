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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.state.disable.app;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.cloud.scheduler.env.BootstrapEnvironment;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Disable app service.
 */
@Slf4j
public class DisableAppService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final CoordinatorRegistryCenter regCenter;
    
    public DisableAppService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    /**
     * Add application name to disable queue.
     *
     * @param appName application name
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
     * Remove application name from disable queue.
     * 
     * @param appName application name
     */
    public void remove(final String appName) {
        regCenter.remove(DisableAppNode.getDisableAppNodePath(appName));
    }
    
    /**
     * Check whether the application name is disabled or not.
     * 
     * @param appName application name
     * @return true is in the disable queue, otherwise not
     */
    public boolean isDisabled(final String appName) {
        return regCenter.isExisted(DisableAppNode.getDisableAppNodePath(appName));
    }
}
