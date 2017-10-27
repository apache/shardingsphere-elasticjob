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

package io.elasticjob.lite.internal.storage;

import lombok.RequiredArgsConstructor;

/**
 * 作业节点路径类.
 * 
 * <p>
 * 作业节点是在普通的节点前加上作业名称的前缀.
 * </p>
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class JobNodePath {
    
    private static final String LEADER_HOST_NODE = "leader/election/instance";
    
    private static final String CONFIG_NODE = "config";
    
    private static final String SERVERS_NODE = "servers";
    
    private static final String INSTANCES_NODE = "instances";
    
    private static final String SHARDING_NODE = "sharding";
    
    private final String jobName;
    
    /**
     * 获取节点全路径.
     * 
     * @param node 节点名称
     * @return 节点全路径
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", jobName, node);
    }
    
    /**
     * 获取配置节点根路径.
     *
     * @return 配置节点根路径
     */
    public String getConfigNodePath() {
        return String.format("/%s/%s", jobName, CONFIG_NODE);
    }
    
    /**
     * 获取leader选举地址节点路径.
     *
     * @return leader选举地址节点路径
     */
    public String getLeaderHostNodePath() {
        return String.format("/%s/%s", jobName, LEADER_HOST_NODE);
    }
    
    /**
     * 获取作业节点IP地址根路径.
     *
     * @return 作业节点IP地址根路径
     */
    public String getServerNodePath() {
        return String.format("/%s/%s", jobName, SERVERS_NODE);
    }
    
    /**
     * 根据IP地址获取作业节点路径.
     *
     * @param serverIp 作业服务器IP地址
     * @return 作业节点IP地址路径
     */
    public String getServerNodePath(final String serverIp) {
        return String.format("%s/%s", getServerNodePath(), serverIp);
    }
    
    /**
     * 获取作业实例节点根路径.
     *
     * @return 作业实例节点根路径
     */
    public String getInstancesNodePath() {
        return String.format("/%s/%s", jobName, INSTANCES_NODE);
    }
    
    /**
     * 根据作业实例ID获取作业实例节点路径.
     *
     * @param instanceId 作业实例ID
     * @return 作业实例节点路径
     */
    public String getInstanceNodePath(final String instanceId) {
        return String.format("%s/%s", getInstancesNodePath(), instanceId);
    }
    
    /**
     * 获取分片节点根路径.
     *
     * @return 分片节点根路径
     */
    public String getShardingNodePath() {
        return String.format("/%s/%s", jobName, SHARDING_NODE);
    }
    
    /**
     * 获取分片节点路径.
     *
     * @param item 分片项
     * @param nodeName 子节点名称
     * @return 分片节点路径
     */
    public String getShardingNodePath(final String item, final String nodeName) {
        return String.format("%s/%s/%s", getShardingNodePath(), item, nodeName);
    }
}
