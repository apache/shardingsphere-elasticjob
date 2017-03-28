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

import com.dangdang.ddframe.job.lite.api.strategy.JobShardingUnit;
import com.dangdang.ddframe.job.lite.internal.config.ConfigurationService;
import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 作业服务器节点服务.
 * 
 * @author zhangliang
 * @author caohao
 */
public class ServerService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ServerNode serverNode;
    
    private final ServerOperationNode serverOperationNode;
    
    private final LocalHostService localHostService = new LocalHostService();
    
    private final ConfigurationService configService;
    
    public ServerService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        serverNode = new ServerNode(jobName);
        serverOperationNode = new ServerOperationNode(jobName);
        configService = new ConfigurationService(regCenter, jobName);
    }
    
    /**
     * 持久化作业服务器上线相关信息.
     * 
     * @param enabled 作业是否启用
     */
    public void persistServerOnline(final boolean enabled) {
        if (enabled) {
            jobNodeStorage.removeJobNodeIfExisted(serverOperationNode.getDisabledNode());
        } else {
            jobNodeStorage.fillJobNode(serverOperationNode.getDisabledNode(), "");
        }
        // TODO 使用临时节点
//        jobNodeStorage.fillEphemeralJobNode(serverNode.getLocalInstanceNode(), new Gson().toJson(new InstanceInfo()));
        jobNodeStorage.fillJobNode(serverNode.getLocalInstanceNode(), new Gson().toJson(new InstanceInfo()));
    }
    
    /**
     * 在开始或结束执行作业时更新服务器状态.
     * 
     * @param status 服务器状态
     */
    public void updateServerStatus(final ServerStatus status) {
        jobNodeStorage.updateJobNode(serverNode.getLocalInstanceNode(), new Gson().toJson(new InstanceInfo(status)));
    }
    
    /**
     * 删除运行实例状态.
     */
    public void removeInstanceStatus() {
        jobNodeStorage.removeJobNodeIfExisted(serverNode.getLocalInstanceNode());
    }
    
    /**
     * 获取所有分片单元列表.
     *
     * @return 所有分片单元列表
     */
    public List<JobShardingUnit> getAllShardingUnits() {
        List<String> servers = getAllServers();
        List<JobShardingUnit> result = new LinkedList<>();
        for (String each : servers) {
            List<String> jobInstances = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT + "/" + each + "/" + ServerNode.INSTANCES_ROOT);
            for (String jobInstanceId : jobInstances) {
                result.add(new JobShardingUnit(each, jobInstanceId));
            }
        }
        return result;
    }
    
    /**
     * 获取可分片的单元列表.
     *
     * @return 可分片的单元列表
     */
    public List<JobShardingUnit> getAvailableShardingUnits() {
        List<String> servers = getAllServers();
        List<JobShardingUnit> result = new LinkedList<>();
        for (String each : servers) {
            List<String> jobInstances = getAvailableInstances(each);
            for (String jobInstanceId : jobInstances) {
                result.add(new JobShardingUnit(each, jobInstanceId));
            }
        }
        return result;
    }
    
    private List<String> getAvailableInstances(final String ip) {
        List<String> result = new LinkedList<>();
        List<String> jobInstances = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT + "/" + ip + "/" + ServerNode.INSTANCES_ROOT);
        for (String each : jobInstances) {
            if (jobNodeStorage.isJobNodeExisted(ServerNode.getInstanceNode(ip, each)) && !jobNodeStorage.isJobNodeExisted(serverOperationNode.getDisabledNode(ip))
                    && !new Gson().fromJson(jobNodeStorage.getJobNodeDataDirectly(ServerNode.getInstanceNode(ip, each)), InstanceInfo.class).isShutdown()) {
                result.add(each);
            }
        }
        return result;
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
     * @param ip 作业服务器IP地址.
     * @return 作业服务器是否可用
     */
    public boolean isAvailableServer(final String ip) {
        List<String> instances = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT + "/" + ip + "/" + ServerNode.INSTANCES_ROOT);
        for (String each : instances) {
            if (jobNodeStorage.isJobNodeExisted(ServerNode.getInstanceNode(ip, each)) && !jobNodeStorage.isJobNodeExisted(serverOperationNode.getDisabledNode(ip))
                    && !new Gson().fromJson(jobNodeStorage.getJobNodeDataDirectly(ServerNode.getInstanceNode(ip, each)), InstanceInfo.class).isShutdown()) {
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
        return isAvailableServer(localHostService.getIp())
                && ServerStatus.READY == new Gson().fromJson(jobNodeStorage.getJobNodeDataDirectly(serverNode.getLocalInstanceNode()), InstanceInfo.class).getServerStatus();
    }
    
    /**
     * 判断当前服务器是否是启用状态.
     *
     * @return 当前服务器是否是启用状态
     */
    public boolean isLocalhostServerEnabled() {
        return !jobNodeStorage.isJobNodeExisted(serverOperationNode.getDisabledNode());
    }
    
    /**
     * 判断作业节点是否离线.
     * 
     * @param ip 作业服务器IP
     * @param jobInstanceId 作业实例主键
     * @return 作业节点是否离线
     */
    public boolean isOffline(final String ip, final String jobInstanceId) {
        return !jobNodeStorage.isJobNodeExisted(ServerNode.getInstanceNode(ip, jobInstanceId));
    }
}
