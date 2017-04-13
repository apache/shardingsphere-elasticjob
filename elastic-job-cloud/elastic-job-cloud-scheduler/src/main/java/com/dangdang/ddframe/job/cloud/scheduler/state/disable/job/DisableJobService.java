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

package com.dangdang.ddframe.job.cloud.scheduler.state.disable.job;

import com.dangdang.ddframe.job.cloud.scheduler.env.BootstrapEnvironment;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.extern.slf4j.Slf4j;

/**
 * 禁用作业队列服务.
 *
 * @author caohao
 */
@Slf4j
public class DisableJobService {
    
    private final BootstrapEnvironment env = BootstrapEnvironment.getInstance();
    
    private final CoordinatorRegistryCenter regCenter;
    
    public DisableJobService(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    /**
     * 将作业放入禁用队列.
     *
     * @param jobName 作业名称
     */
    public void add(final String jobName) {
        if (regCenter.getNumChildren(DisableJobNode.ROOT) > env.getFrameworkConfiguration().getJobStateQueueSize()) {
            log.warn("Cannot add disable job, caused by read state queue size is larger than {}.", env.getFrameworkConfiguration().getJobStateQueueSize());
            return;
        }
        String disableJobNodePath = DisableJobNode.getDisableJobNodePath(jobName);
        if (!regCenter.isExisted(disableJobNodePath)) {
            regCenter.persist(disableJobNodePath, jobName);
        }
    }
    
    /**
     * 从作业禁用队列中删除作业.
     *
     * @param jobName 待删除的作业名称
     */
    public void remove(final String jobName) {
        regCenter.remove(DisableJobNode.getDisableJobNodePath(jobName));
    }
    
    /**
     * 判断作业是否在作业禁用队列中.
     *
     * @param jobName 作业名称
     * @return 作业是否被禁用
     */
    public boolean isDisabled(final String jobName) {
        return regCenter.isExisted(DisableJobNode.getDisableJobNodePath(jobName));
    }
}
