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
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 作业服务器状态展示的实现类.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class ServerStatisticsAPIImpl implements ServerStatisticsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public int getServersTotalCount() {
        Set<String> servers = new HashSet<>();
        for (String jobName : regCenter.getChildrenKeys("/")) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
                servers.add(each);
            }
        }
        return servers.size();
    }
    
    @Override
    public Collection<ServerBriefInfo> getAllServersBriefInfo() {
        ConcurrentHashMap<String, ServerBriefInfo> servers = new ConcurrentHashMap<>();
        for (String jobName : regCenter.getChildrenKeys("/")) {
            JobNodePath jobNodePath = new JobNodePath(jobName);
            for (String each : regCenter.getChildrenKeys(jobNodePath.getServerNodePath())) {
                servers.putIfAbsent(each, new ServerBriefInfo(each));
                ServerBriefInfo serverInfo = servers.get(each);
                if ("DISABLED".equalsIgnoreCase(regCenter.get(jobNodePath.getServerNodePath(each)))) {
                    serverInfo.getDisabledJobsNum().incrementAndGet();
                }
                serverInfo.getJobNames().add(jobName);
                serverInfo.setJobsNum(serverInfo.getJobNames().size());
            }
            List<String> instances = regCenter.getChildrenKeys(jobNodePath.getInstancesNodePath());
            for (String each : instances) {
                String serverIp = each.split("@-@")[0];
                ServerBriefInfo serverInfo = servers.get(serverIp);
                if (null != serverInfo) {
                    serverInfo.getInstances().add(each);
                    serverInfo.setInstancesNum(serverInfo.getInstances().size());
                }
            }
        }
        List<ServerBriefInfo> result = new ArrayList<>(servers.values());
        Collections.sort(result);
        return result;
    }
}
