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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.internal.instance.InstanceNode;
import com.dangdang.ddframe.job.lite.internal.instance.InstanceStatus;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作业服务器服务.
 * 
 * @author zhangliang
 * @author caohao
 */
public class ServerService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final InstanceNode instanceNode;
    
    private final ServerNode serverNode;
    
    public ServerService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        instanceNode = new InstanceNode(jobName);
        serverNode = new ServerNode(jobName);
    }
    
    /**
     * 持久化作业服务器上线相关信息.
     * 
     * @param enabled 作业是否启用
     */
    public void persistServerOnline(final boolean enabled) {
        jobNodeStorage.fillJobNode(serverNode.getServerNode(), enabled ? "" : ServerStatus.DISABLED.name());
    }
    
    /**
     * 获取可用的作业服务器列表.
     * 
     * @return 可用的作业服务器列表
     */
    public List<String> getAvailableServers() {
        List<String> servers = getAllServers();
        List<String> result = new ArrayList<>(servers.size());
        for (String each : servers) {
            if (isAvailableServer(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private List<String> getAllServers() {
        List<String> result = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT);
        Collections.sort(result);
        return result;
    }
    
    /**
     * 判断作业服务器是否可用.
     * 
     * @param ip 作业服务器IP地址
     * @return 作业服务器是否可用
     */
    public boolean isAvailableServer(final String ip) {
        return isServerEnabled(ip) && hasOnlineInstances(ip);
    }
    
    /**
     * 判断服务器是否启用.
     *
     * @param ip 作业服务器IP地址
     * @return 服务器是否启用
     */
    public boolean isServerEnabled(final String ip) {
        return !ServerStatus.DISABLED.name().equals(jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip)));
    }
    
    private boolean hasOnlineInstances(final String ip) {
        for (String each : jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT)) {
            if (each.startsWith(ip)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断当前服务器是否是等待执行的状态.
     * 
     * @return 当前服务器是否是等待执行的状态
     */
    public boolean isLocalhostServerReady() {
        return isAvailableServer(JobRegistry.getInstance().getJobInstance(jobName).getIp()) && jobNodeStorage.isJobNodeExisted(instanceNode.getLocalInstanceNode())
                && InstanceStatus.READY.name().equals(jobNodeStorage.getJobNodeDataDirectly(instanceNode.getLocalInstanceNode()));
    }
}
