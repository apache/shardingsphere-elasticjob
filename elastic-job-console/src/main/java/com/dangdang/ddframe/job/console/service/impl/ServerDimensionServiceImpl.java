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

package com.dangdang.ddframe.job.console.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.dangdang.ddframe.job.console.domain.JobServer;
import com.dangdang.ddframe.job.console.domain.JobServer.ServerStatus;
import com.dangdang.ddframe.job.console.domain.ServerBriefInfo;
import com.dangdang.ddframe.job.console.domain.ServerBriefInfo.ServerBriefStatus;
import com.dangdang.ddframe.job.console.repository.zookeeper.CuratorRepository;
import com.dangdang.ddframe.job.console.service.ServerDimensionService;
import com.dangdang.ddframe.job.console.util.JobNodePath;

@Service
public class ServerDimensionServiceImpl implements ServerDimensionService {
    
    @Resource
    private CuratorRepository curatorRepository;
    
    @Override
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        Map<String, String> serverHostMap = new HashMap<>();
        Map<String, Boolean> serverAlivedCountMap = new HashMap<>();
        Map<String, Boolean> serverCrashedCountMap = new HashMap<>();
        List<String> jobs = curatorRepository.getChildren("/");
        for (String jobName : jobs) {
            List<String> servers = curatorRepository.getChildren(JobNodePath.getServerNodePath(jobName));
            for (String server : servers) {
                serverHostMap.put(server, curatorRepository.getData(JobNodePath.getServerNodePath(jobName, server, "hostName")));
                if (curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, server, "status"))) {
                    serverAlivedCountMap.put(server, true);
                } else {
                    serverCrashedCountMap.put(server, true);
                }
            }
        }
        List<ServerBriefInfo> result = new ArrayList<>();
        for (Entry<String, String> entry : serverHostMap.entrySet()) {
            result.add(getServerBriefInfo(serverAlivedCountMap, serverCrashedCountMap, entry));
        }
        Collections.sort(result);
        return result;
    }
    
    private ServerBriefInfo getServerBriefInfo(final Map<String, Boolean> serverAlivedCountMap, final Map<String, Boolean> serverCrashedCountMap, final Entry<String, String> entry) {
        ServerBriefInfo result = new ServerBriefInfo();
        result.setServerIp(entry.getKey());
        result.setServerHostName(entry.getValue());
        if (!serverAlivedCountMap.containsKey(entry.getKey())) {
            result.setStatus(ServerBriefStatus.ALL_CRASHED);
        } else if (!serverCrashedCountMap.containsKey(entry.getKey())) {
            result.setStatus(ServerBriefStatus.OK);
        } else {
            result.setStatus(ServerBriefStatus.PARTIAL_ALIVE);
        }
        return result;
    }
    
    @Override
    public Collection<JobServer> getJobs(final String serverIp) {
        List<String> jobs = curatorRepository.getChildren("/");
        Collection<JobServer> result = new ArrayList<>(jobs.size());
        for (String each : jobs) {
            result.add(getJob(serverIp, each));
        }
        return result;
    }
    
    private JobServer getJob(final String serverIp, final String jobName) {
        JobServer result = new JobServer();
        result.setJobName(jobName);
        result.setIp(serverIp);
        result.setHostName(curatorRepository.getData(JobNodePath.getServerNodePath(jobName, serverIp, "hostName")));
        String processSuccessCount = curatorRepository.getData(JobNodePath.getServerNodePath(jobName, serverIp, "processSuccessCount"));
        result.setProcessSuccessCount(null == processSuccessCount ? 0 : Integer.parseInt(processSuccessCount));
        String processFailureCount = curatorRepository.getData(JobNodePath.getServerNodePath(jobName, serverIp, "processFailureCount"));
        result.setProcessFailureCount(null == processFailureCount ? 0 : Integer.parseInt(processFailureCount));
        result.setSharding(curatorRepository.getData(JobNodePath.getServerNodePath(jobName, serverIp, "sharding")));
        String status = curatorRepository.getData(JobNodePath.getServerNodePath(jobName, serverIp, "status"));
        boolean disabled = curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, serverIp, "disabled"));
        boolean stoped = curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, serverIp, "stoped"));
        result.setStatus(ServerStatus.getServerStatus(status, disabled, stoped));
        String leaderIp = curatorRepository.getData(JobNodePath.getLeaderNodePath(jobName, "election/host"));
        result.setLeader(serverIp.equals(leaderIp));
        result.setLeaderStoped(curatorRepository.checkExists(JobNodePath.getServerNodePath(jobName, leaderIp, "stoped")));
        return result;
    }
}
