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

package io.elasticjob.lite.internal.server;

import io.elasticjob.lite.internal.instance.InstanceNode;
import io.elasticjob.lite.internal.schedule.JobRegistry;
import io.elasticjob.lite.internal.storage.JobNodeStorage;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;

import java.util.List;

/**
 * 作业服务器服务.
 * 
 * @author zhangliang
 * @author caohao
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
     * 持久化作业服务器上线信息.
     * 
     * @param enabled 作业是否启用
     */
    public void persistOnline(final boolean enabled) {
        if (!JobRegistry.getInstance().isShutdown(jobName)) {
            jobNodeStorage.fillJobNode(serverNode.getServerNode(JobRegistry.getInstance().getJobInstance(jobName).getIp()), enabled ? "" : ServerStatus.DISABLED.name());
        }
    }
    
    /**
     * 获取是否还有可用的作业服务器.
     * 
     * @return 是否还有可用的作业服务器
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
     * 判断作业服务器是否可用.
     * 
     * @param ip 作业服务器IP地址
     * @return 作业服务器是否可用
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
     * 判断服务器是否启用.
     *
     * @param ip 作业服务器IP地址
     * @return 服务器是否启用
     */
    public boolean isEnableServer(final String ip) {
        return !ServerStatus.DISABLED.name().equals(jobNodeStorage.getJobNodeData(serverNode.getServerNode(ip)));
    }
}
