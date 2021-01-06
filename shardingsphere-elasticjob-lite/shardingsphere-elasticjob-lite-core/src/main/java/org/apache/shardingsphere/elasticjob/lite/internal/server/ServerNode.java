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

package org.apache.shardingsphere.elasticjob.lite.internal.server;

import org.apache.shardingsphere.elasticjob.infra.handler.sharding.JobInstance;
import org.apache.shardingsphere.elasticjob.lite.internal.schedule.JobRegistry;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodePath;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Server node.
 */
public final class ServerNode {
    
    public static final String ROOT = "servers";
    
    private static final String SERVERS = ROOT + "/%s";
    
    private final String jobName;
    
    private final JobNodePath jobNodePath;
    
    public ServerNode(final String jobName) {
        this.jobName = jobName;
        jobNodePath = new JobNodePath(jobName);
    }
    
    /**
     * Judge is server path or not.
     *
     * @param path path to be judged
     * @return is server path or not
     */
    public boolean isServerPath(final String path) {
        return Pattern.compile(jobNodePath.getFullPath(ServerNode.ROOT) + "/" + IpUtils.IP_REGEX).matcher(path).matches();
    }
    
    /**
     * Judge is server path for localhost or not.
     *
     * @param path path to be judged
     * @return is server path for localhost or not
     */
    public boolean isLocalServerPath(final String path) {
        JobInstance jobInstance = JobRegistry.getInstance().getJobInstance(jobName);
        if (Objects.isNull(jobInstance)) {
            return false;
        }
        return path.equals(jobNodePath.getFullPath(String.format(SERVERS, jobInstance.getIp())));
    }
    
    String getServerNode(final String ip) {
        return String.format(SERVERS, ip);
    }
}
