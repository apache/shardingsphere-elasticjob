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

/**
 * Elastic Job服务器节点操作常量类.
 * 
 * @author zhangliang
 */
public class ServerOperationNode {
    
    static final String OPERATION_ROOT = "operation/";
    
    static final String ROOT = ServerNode.ROOT + "/%s/" + OPERATION_ROOT;
    
    static final String DISABLED_APPENDIX = "disabled";
    
    static final String DISABLED = ROOT + DISABLED_APPENDIX;
    
    static final String SHUTDOWN_APPENDIX = "shutdown";
    
    static final String SHUTDOWN = ROOT + SHUTDOWN_APPENDIX;
    
    private final String ip;
    
    private final JobNodePath jobNodePath;
    
    public ServerOperationNode(final String jobName) {
        ip = new LocalHostService().getIp();
        jobNodePath = new JobNodePath(jobName);
    }
    
    String getDisabledNode() {
        return getDisabledNode(ip);
    }
    
    String getDisabledNode(final String ip) {
        return String.format(DISABLED, ip);
    }
        
    String getShutdownNode() {
        return getShutdownNode(ip);
    }
    
    String getShutdownNode(final String ip) {
        return String.format(SHUTDOWN, ip);
    }
    
    /**
     * 判断给定路径是否为作业服务器关闭路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器关闭路径
     */
    public boolean isLocalJobShutdownPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerOperationNode.SHUTDOWN, ip)));
    }
    
    /**
     * 判断给定路径是否为作业服务器禁用路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器禁用路径
     */
    public boolean isLocalServerDisabledPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerOperationNode.DISABLED, ip)));
    }
    
    /**
     * 判断给定路径是否为作业服务器禁用路径.
     * 
     * @param path 待判断的路径
     * @return 是否为作业服务器禁用路径
     */
    public boolean isServerDisabledPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ServerNode.ROOT)) && path.endsWith(ServerOperationNode.OPERATION_ROOT + ServerOperationNode.DISABLED_APPENDIX);
    }
    
    /**
     * 判断给定路径是否为作业服务器关闭路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器关闭路径
     */
    public boolean isServerShutdownPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ServerNode.ROOT)) && path.endsWith(ServerOperationNode.OPERATION_ROOT + ServerOperationNode.SHUTDOWN_APPENDIX);
    }
}
