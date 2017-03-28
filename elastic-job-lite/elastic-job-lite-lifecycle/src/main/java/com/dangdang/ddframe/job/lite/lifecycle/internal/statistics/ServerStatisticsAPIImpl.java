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

package com.dangdang.ddframe.job.lite.lifecycle.internal.statistics;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.api.ServerStatisticsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.lite.lifecycle.domain.ServerInfo;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 作业服务器状态展示的实现类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ServerStatisticsAPIImpl implements ServerStatisticsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        Set<String> serverInstances = new HashSet<>();
        Collection<String> aliveServers = new ArrayList<>();
        Collection<String> crashedServers = new ArrayList<>();
        List<String> jobs = regCenter.getChildrenKeys("/");
        for (String jobName : jobs) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
            
            for (String server : servers) {
                List<String> jobInstances = regCenter.getChildrenKeys(jobNodePath.getServerNodePath(server));
                for (String jobInstance : jobInstances) {
                    String identifier = server + "-" + jobInstance;
                    serverInstances.add(identifier);
                    if (!regCenter.isExisted(jobNodePath.getServerInstanceNodePath(server, jobInstance, "shutdown")) 
                            && regCenter.isExisted(jobNodePath.getServerInstanceNodePath(server, jobInstance, "status"))) {
                        aliveServers.add(identifier);
                    } else {
                        crashedServers.add(identifier);
                    }
                }
            }
        }
        List<ServerBriefInfo> result = new ArrayList<>(serverInstances.size());
        for (String each : serverInstances) {
            result.add(getServerBriefInfo(aliveServers, crashedServers, each));
        }
        Collections.sort(result);
        return result;
    }
    
    private ServerBriefInfo getServerBriefInfo(final Collection<String> aliveServers, final Collection<String> crashedServers, final String key) {
        ServerBriefInfo result = new ServerBriefInfo();
        String serverIp = key.split("-")[0];
        String instanceId = key.split("-")[1];
        result.setServerIp(serverIp);
        result.setInstanceId(instanceId);
        result.setStatus(ServerBriefInfo.ServerBriefStatus.getServerBriefStatus(aliveServers, crashedServers, serverIp, instanceId));
        return result;
    }
    
    @Override
    public Collection<ServerInfo> getJobs(final String serverIp, final String instanceId) {
        List<String> jobs = regCenter.getChildrenKeys("/");
        Collection<ServerInfo> result = new ArrayList<>(jobs.size());
        for (String each : jobs) {
            JobNodePath jobNodePath = new JobNodePath(each);
            if (regCenter.isExisted(jobNodePath.getServerInstanceNodePath(serverIp, instanceId))) {
                result.add(getJob(serverIp, instanceId, each));
            }
        }
        return result;
    }
    
    private ServerInfo getJob(final String serverIp, final String instanceId, final String jobName) {
        ServerInfo result = new ServerInfo();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        result.setJobName(jobName);
        result.setIp(serverIp);
        result.setInstanceId(instanceId);
        result.setSharding(regCenter.get(jobNodePath.getServerInstanceNodePath(serverIp, instanceId, "sharding")));
        String status = regCenter.get(jobNodePath.getServerInstanceNodePath(serverIp, instanceId, "status"));
        boolean disabled = regCenter.isExisted(jobNodePath.getServerInstanceNodePath(serverIp, instanceId, "disabled"));
        boolean shutdown = regCenter.isExisted(jobNodePath.getServerInstanceNodePath(serverIp, instanceId, "shutdown"));
        result.setStatus(ServerInfo.ServerStatus.getServerStatus(status, disabled, shutdown));
        return result;
    }
}
