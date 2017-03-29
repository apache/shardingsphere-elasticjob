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

package com.dangdang.ddframe.job.lite.internal.server;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.util.env.LocalHostService;

import java.util.regex.Pattern;

/**
 * Elastic Job服务器节点操作常量类.
 * 
 * @author zhangliang
 */
public class ServerOperationNode {
    
    private final String ip;
    
    private final JobNodePath jobNodePath;
    
    public ServerOperationNode(final String jobName) {
        ip = new LocalHostService().getIp();
        jobNodePath = new JobNodePath(jobName);
    }
    
    String getServerNode() {
        return getServerNode(ip);
    }
    
    String getServerNode(final String ip) {
        return String.format(ServerNode.ROOT + "/%s", ip);
    }
        
    /**
     * 判断给定路径是否为作业服务器路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器路径
     */
    public boolean isLocalServerPath(final String path) {
        return path.equals(jobNodePath.getFullPath(String.format(ServerNode.ROOT + "/%s", ip)));
    }
    
    /**
     * 判断给定路径是否为作业服务器路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器路径
     */
    public boolean isServerPath(final String path) {
        return Pattern.compile(jobNodePath.getFullPath(ServerNode.ROOT) + "/" + LocalHostService.IP_REGEX).matcher(path).matches();
    }
}
