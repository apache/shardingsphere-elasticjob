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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> serverHostMap = new HashMap<>();
        Collection<String> aliveServers = new ArrayList<>();
        Collection<String> crashedServers = new ArrayList<>();
        List<String> jobs = regCenter.getChildrenKeys("/");
        for (String jobName : jobs) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            List<String> servers = regCenter.getChildrenKeys(jobNodePath.getServerNodePath());
            for (String server : servers) {
                serverHostMap.put(server, regCenter.get(jobNodePath.getServerNodePath(server, "hostName")));
                if (!regCenter.isExisted(jobNodePath.getServerNodePath(server, "shutdown")) && regCenter.isExisted(jobNodePath.getServerNodePath(server, "status"))) {
                    aliveServers.add(server);
                } else {
                    crashedServers.add(server);
                }
            }
        }
        List<ServerBriefInfo> result = new ArrayList<>(serverHostMap.size());
        for (Map.Entry<String, String> entry : serverHostMap.entrySet()) {
            result.add(getServerBriefInfo(aliveServers, crashedServers, entry.getKey(), entry.getValue()));
        }
        Collections.sort(result);
        return result;
    }
    
    private ServerBriefInfo getServerBriefInfo(final Collection<String> aliveServers, final Collection<String> crashedServers, final String serverIp, final String hostName) {
        ServerBriefInfo result = new ServerBriefInfo();
        result.setServerIp(serverIp);
        result.setServerHostName(hostName);
        result.setStatus(ServerBriefInfo.ServerBriefStatus.getServerBriefStatus(aliveServers, crashedServers, serverIp));
        return result;
    }
    
    @Override
    public Collection<ServerInfo> getJobs(final String serverIp) {
        List<String> jobs = regCenter.getChildrenKeys("/");
        Collection<ServerInfo> result = new ArrayList<>(jobs.size());
        for (String each : jobs) {
            JobNodePath jobNodePath = new JobNodePath(each);
            if (regCenter.isExisted(jobNodePath.getServerNodePath(serverIp))) {
                result.add(getJob(serverIp, each));
            }
        }
        return result;
    }
    
    private ServerInfo getJob(final String serverIp, final String jobName) {
        ServerInfo result = new ServerInfo();
        JobNodePath jobNodePath = new JobNodePath(jobName);
        result.setJobName(jobName);
        result.setIp(serverIp);
        result.setHostName(regCenter.get(jobNodePath.getServerNodePath(serverIp, "hostName")));
        result.setSharding(regCenter.get(jobNodePath.getServerNodePath(serverIp, "sharding")));
        String status = regCenter.get(jobNodePath.getServerNodePath(serverIp, "status"));
        boolean disabled = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "disabled"));
        boolean paused = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "paused"));
        boolean shutdown = regCenter.isExisted(jobNodePath.getServerNodePath(serverIp, "shutdown"));
        result.setStatus(ServerInfo.ServerStatus.getServerStatus(status, disabled, paused, shutdown));
        return result;
    }
}
