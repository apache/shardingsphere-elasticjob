/**
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

package com.dangdang.ddframe.job.internal.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dangdang.ddframe.job.api.JobConfiguration;
import com.dangdang.ddframe.job.internal.election.LeaderElectionService;
import com.dangdang.ddframe.job.internal.env.LocalHostService;
import com.dangdang.ddframe.job.internal.env.RealLocalHostService;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业服务器节点服务.
 * 
 * @author zhangliang, caohao
 */
public final class ServerService {
    
    private final JobNodeStorage jobNodeStorage;
    
    private final LocalHostService localHostService = new RealLocalHostService();
    
    private final LeaderElectionService leaderElectionService;
    
    public ServerService(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
        leaderElectionService = new LeaderElectionService(coordinatorRegistryCenter, jobConfiguration);
    }
    
    /**
     * 持久化作业服务器上线相关信息.
     */
    public void persistServerOnline() {
        if (!leaderElectionService.hasLeader()) {
            leaderElectionService.leaderElection();
        }
        persistHostName();
        persistDisabled();
        ephemeralPersistServerReady();
    }
    
    private void persistHostName() {
        jobNodeStorage.fillJobNodeIfNullOrOverwrite(ServerNode.getHostNameNode(localHostService.getIp()), localHostService.getHostName());
    }
    
    private void persistDisabled() {
        if (!jobNodeStorage.getJobConfiguration().isOverwrite()) {
            return;
        }
        if (jobNodeStorage.getJobConfiguration().isDisabled()) {
            jobNodeStorage.fillJobNodeIfNullOrOverwrite(ServerNode.getDisabledNode(localHostService.getIp()), "");
        } else {
            jobNodeStorage.removeJobNodeIfExisted(ServerNode.getDisabledNode(localHostService.getIp()));
        }
    }
    
    private void ephemeralPersistServerReady() {
        jobNodeStorage.fillEphemeralJobNode(ServerNode.getStatusNode(localHostService.getIp()), ServerStatus.READY);
    }
    
    /**
     * 清除停止作业的标记.
     */
    public void clearJobStopedStatus() {
        jobNodeStorage.removeJobNodeIfExisted(ServerNode.getStopedNode(localHostService.getIp()));
    }
    
    /**
     * 判断是否是手工停止的作业.
     * 
     * @return 是否是手工停止的作业
     */
    public boolean isJobStopedManually() {
        return jobNodeStorage.isJobNodeExisted(ServerNode.getStopedNode(localHostService.getIp()));
    }
    
    /**
     * 在开始或结束执行作业时更新服务器状态.
     * 
     * @param status 服务器状态
     */
    public void updateServerStatus(final ServerStatus status) {
        jobNodeStorage.updateJobNode(ServerNode.getStatusNode(localHostService.getIp()), status);
    }
    
    /**
     * 获取所有的作业服务器列表.
     * 
     * @return 所有的作业服务器列表
     */
    public List<String> getAllServers() {
        List<String> result = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT);
        Collections.sort(result);
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
        Collections.sort(result);
        return result;
    }
    
    private Boolean isAvailableServer(final String ip) {
        return jobNodeStorage.isJobNodeExisted(ServerNode.getStatusNode(ip)) && !jobNodeStorage.isJobNodeExisted(ServerNode.getDisabledNode(ip));
    }
    
    /**
     * 判断当前服务器是否是等待执行的状态.
     * 
     * @return 当前服务器是否是等待执行的状态
     */
    public boolean isServerReady() {
        if (jobNodeStorage.isJobNodeExisted(ServerNode.getDisabledNode(localHostService.getIp()))) {
            return false;
        }
        if (jobNodeStorage.isJobNodeExisted(ServerNode.getStopedNode(localHostService.getIp()))) {
            return false;
        }
        String statusNode = ServerNode.getStatusNode(localHostService.getIp());
        if (jobNodeStorage.isJobNodeExisted(statusNode) && ServerStatus.READY.name().equals(jobNodeStorage.getJobNodeData(statusNode))) {
            return true;
        }
        return false;
    }
    
    /**
     * 持久化统计处理数据成功的数量的数据.
     */
    public void persistProcessSuccessCount(final int processSuccessCount) {
        jobNodeStorage.replaceJobNode(ServerNode.getProcessSuccessCountNode(localHostService.getIp()), processSuccessCount);
    }
    
    /**
     * 持久化统计处理数据失败的数量的数据.
     */
    public void persistProcessFailureCount(final int processFailureCount) {
        jobNodeStorage.replaceJobNode(ServerNode.getProcessFailureCountNode(localHostService.getIp()), processFailureCount);
    }
}
