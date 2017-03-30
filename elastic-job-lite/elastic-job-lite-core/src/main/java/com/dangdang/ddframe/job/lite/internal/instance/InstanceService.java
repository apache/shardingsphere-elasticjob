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

package com.dangdang.ddframe.job.lite.internal.instance;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

import java.util.LinkedList;
import java.util.List;

/**
 * 作业运行实例服务.
 * 
 * @author zhangliang
 */
public class InstanceService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final InstanceNode instanceNode;
    
    private final ServerService serverService;
    
    public InstanceService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        instanceNode = new InstanceNode(jobName);
        serverService = new ServerService(regCenter, jobName);
    }
    
    /**
     * 持久化作业运行实例上线相关信息.
     */
    public void persistOnline() {
        jobNodeStorage.fillEphemeralJobNode(instanceNode.getLocalInstanceNode(), InstanceStatus.READY.name());
    }
    
    /**
     * 更新作业运行状态.
     * 
     * @param status 作业运行状态
     */
    public void updateStatus(final InstanceStatus status) {
        jobNodeStorage.updateJobNode(instanceNode.getLocalInstanceNode(), status.name());
    }
    
    /**
     * 删除作业运行状态.
     */
    public void removeStatus() {
        jobNodeStorage.removeJobNodeIfExisted(instanceNode.getLocalInstanceNode());
    }
    
    /**
     * 获取可分片的单元列表.
     *
     * @return 可分片的单元列表
     */
    public List<JobInstance> getAvailableJobInstances() {
        List<JobInstance> result = new LinkedList<>();
        for (String each : jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)) {
            JobInstance shardingUnit = new JobInstance(each);
            if (serverService.isServerEnabled(shardingUnit.getIp())) {
                result.add(new JobInstance(each));
            }
        }
        return result;
    }
}
