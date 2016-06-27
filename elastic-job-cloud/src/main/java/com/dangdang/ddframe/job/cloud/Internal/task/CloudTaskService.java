/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.Internal.task;

import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

/**
 * 云任务服务.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class CloudTaskService {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    /**
     * 添加云任务.
     * 
     * @param task 云任务对象
     */
    // TODO 应为API
    public void addTask(final CloudTask task) {
        CloudTaskNode cloudTaskNode = new CloudTaskNode(task.getJobName());
        registryCenter.persist(cloudTaskNode.getShardingTotalCountNodePath(), Integer.toString(task.getShardingTotalCount()));
        registryCenter.persist(cloudTaskNode.getCpuCountNodePath(), Double.toString(task.getCpuCount()));
        registryCenter.persist(cloudTaskNode.getMemoryMBNodePath(), Integer.toString(task.getMemoryMB()));
        registryCenter.persist(cloudTaskNode.getDockerImageNameNodePath(), task.getDockerImageName());
        registryCenter.persist(cloudTaskNode.getAppURLNodePath(), task.getAppURL());
        registryCenter.persist(cloudTaskNode.getConnectStringNodePath(), task.getConnectString());
        registryCenter.persist(cloudTaskNode.getNamespaceNodePath(), task.getNamespace());
        if (task.getDigest().isPresent()) {
            registryCenter.persist(cloudTaskNode.getCpuCountNodePath(), task.getDigest().get());
        }
        registryCenter.persist(cloudTaskNode.getCronNodePath(), task.getCron());
    }
    
    /**
     * 根据作业名称获取云任务对象.
     * 
     * @param jobName 作业名称
     * @return 云任务对象
     */
    public CloudTask getTask(final String jobName) {
        CloudTaskNode cloudTaskNode = new CloudTaskNode(jobName);
        Preconditions.checkState(registryCenter.isExisted(cloudTaskNode.getRootNodePath()));
        return new CloudTask(
                jobName, 
                registryCenter.get(cloudTaskNode.getCronNodePath()), 
                Integer.parseInt(registryCenter.get(cloudTaskNode.getShardingTotalCountNodePath())), 
                Double.parseDouble(registryCenter.get(cloudTaskNode.getCpuCountNodePath())), 
                Integer.parseInt(registryCenter.get(cloudTaskNode.getMemoryMBNodePath())), 
                registryCenter.get(cloudTaskNode.getDockerImageNameNodePath()),
                registryCenter.get(cloudTaskNode.getAppURLNodePath()),
                registryCenter.get(cloudTaskNode.getConnectStringNodePath()),
                registryCenter.get(cloudTaskNode.getNamespaceNodePath()),
                registryCenter.isExisted(cloudTaskNode.getDigestNodePath()) ? Optional.of(registryCenter.get(cloudTaskNode.getDigestNodePath())) : Optional.<String>absent());
    }
}
