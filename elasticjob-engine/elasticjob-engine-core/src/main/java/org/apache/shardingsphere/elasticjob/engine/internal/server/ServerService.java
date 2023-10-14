/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.engine.internal.server;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.elasticjob.infra.concurrent.BlockUtils;
import org.apache.shardingsphere.elasticjob.engine.internal.instance.InstanceNode;
import org.apache.shardingsphere.elasticjob.engine.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.engine.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Server service.
 */
public final class ServerService {
    
    private final String jobName;
    
    private final JobNodeStorage jobNodeStorage;
    
    private final ServerNode serverNode;
    
    public ServerService(final CoordinatorRegistryCenter regCenter, final String jobName) {
        this.jobName = jobName;
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        serverNode = new ServerNode(jobName);
    }
    
    /**
     * Persist online status of job server.
     *
     * @param enabled enable server or not
     */
    public void persistOnline(final boolean enabled) {
        if (!JobRegistry.getInstance().isShutdown(jobName)) {
            jobNodeStorage.fillJobNode(serverNode.getServerNode(JobRegistry.getInstance().getJobInstance(jobName).getServerIp()), enabled ? ServerStatus.ENABLED.name() : ServerStatus.DISABLED.name());
        }
    }
    
    /**
     * Judge has available servers or not.
     *
     * @return has available servers or not
     */
    public boolean hasAvailableServers() {
        List<String> servers = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT);
        for (String each : servers) {
            if (isAvailableServer(each)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge is available server or not.
     *
     * @param ip job server IP address
     * @return is available server or not
     */
    public boolean isAvailableServer(final String ip) {
        return isEnableServer(ip) && hasOnlineInstances(ip);
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
     * Judge is server enabled or not.
     *
     * @param ip job server IP address
     * @return is server enabled or not
     */
    public boolean isEnableServer(final String ip) {
        String serverStatus = jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip));
        for (int i = 0; Strings.isNullOrEmpty(serverStatus) && i < 10; i++) {
            BlockUtils.waitingShortTime();
            serverStatus = jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip));
        }
        return ServerStatus.ENABLED.name().equals(serverStatus);
    }
    
    /**
     *  Remove unused server IP.
     *  
     * @return num of server IP to be removed
     */
    public int removeOfflineServers() {
        AtomicInteger affectNums = new AtomicInteger();
        Collection<String> instances = jobNodeStorage.getJobNodeChildrenKeys(InstanceNode.ROOT);
        if (instances == null || instances.isEmpty()) {
            return affectNums.get();
        }
        Collection<String> instanceIps = instances.stream().map(instance -> instance.split("@-@")[0]).collect(Collectors.toSet());
        if (instanceIps.isEmpty()) {
            return affectNums.get();
        }
        List<String> serverIps = jobNodeStorage.getJobNodeChildrenKeys(ServerNode.ROOT);
        if (serverIps == null || serverIps.isEmpty()) {
            return affectNums.get();
        }
        
        serverIps.forEach(serverIp -> {
            if (instanceIps.contains(serverIp)) {
                return;
            }
            String status = jobNodeStorage.getJobNodeData(serverNode.getServerNode(serverIp));
            if (StringUtils.isNotBlank(status)) {
                return;
            }
            jobNodeStorage.removeJobNodeIfExisted(serverNode.getServerNode(serverIp));
            affectNums.getAndIncrement();
        });
        return affectNums.get();
    }
}
