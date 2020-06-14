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

package org.apache.shardingsphere.elasticjob.lite.internal.storage;

import lombok.RequiredArgsConstructor;

/**
 * Job node path.
 * 
 * <p>
 * Job node is add job name as prefix.
 * </p>
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
     * Get full path.
     * 
     * @param node node
     * @return full path
     */
    public String getFullPath(final String node) {
        return String.format("/%s/%s", jobName, node);
    }
    
    /**
     * Get configuration node path.
     *
     * @return configuration node path
     */
    public String getConfigNodePath() {
        return String.format("/%s/%s", jobName, CONFIG_NODE);
    }
    
    /**
     * Get leader host node path.
     *
     * @return leader host node path
     */
    public String getLeaderHostNodePath() {
        return String.format("/%s/%s", jobName, LEADER_HOST_NODE);
    }
    
    /**
     * Get server node path.
     *
     * @return server node path
     */
    public String getServerNodePath() {
        return String.format("/%s/%s", jobName, SERVERS_NODE);
    }
    
    /**
     * Get server node path.
     *
     * @param serverIp server IP address
     * @return server node path
     */
    public String getServerNodePath(final String serverIp) {
        return String.format("%s/%s", getServerNodePath(), serverIp);
    }
    
    /**
     * Get instances node path.
     *
     * @return instances node path
     */
    public String getInstancesNodePath() {
        return String.format("/%s/%s", jobName, INSTANCES_NODE);
    }
    
    /**
     * Get instance node path via job instance ID.
     *
     * @param instanceId instance ID
     * @return instance node path
     */
    public String getInstanceNodePath(final String instanceId) {
        return String.format("%s/%s", getInstancesNodePath(), instanceId);
    }
    
    /**
     * Get sharding node path.
     *
     * @return sharding node path
     */
    public String getShardingNodePath() {
        return String.format("/%s/%s", jobName, SHARDING_NODE);
    }
    
    /**
     * Get sharding node path.
     *
     * @param item sharding item
     * @param nodeName node name
     * @return sharding node path
     */
    public String getShardingNodePath(final String item, final String nodeName) {
        return String.format("%s/%s/%s", getShardingNodePath(), item, nodeName);
    }
}
