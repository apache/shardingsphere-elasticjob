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
 * Elastic Job服务器节点名称的常量类.
 * 
 * @author zhangliang
 */
public class ServerNode {
    
    /**
     * 作业服务器信息根节点.
     */
    public static final String ROOT = "servers";
    
    static final String HOST_NAME = ROOT + "/%s/hostName";
    
    static final String STATUS_APPENDIX = "status";
    
    static final String STATUS = ROOT + "/%s/" + STATUS_APPENDIX;
    
    static final String TRIGGER_APPENDIX = "trigger";
    
    static final String TRIGGER = ROOT + "/%s/" + TRIGGER_APPENDIX;
    
    static final String DISABLED_APPENDIX = "disabled";
    
    static final String DISABLED = ROOT + "/%s/" + DISABLED_APPENDIX;
    
    static final String PAUSED = ROOT + "/%s/paused";
    
    static final String SHUTDOWN_APPENDIX = "shutdown";
    
    static final String SHUTDOWN = ROOT + "/%s/" + SHUTDOWN_APPENDIX;
    
    private final LocalHostService localHostService = new LocalHostService();
    
    private final JobNodePath jobNodePath;
    
    public ServerNode(final String jobName) {
        jobNodePath = new JobNodePath(jobName);
    }
    
    static String getHostNameNode(final String ip) {
        return String.format(HOST_NAME, ip);
    }
    
    static String getStatusNode(final String ip) {
        return String.format(STATUS, ip);
    }
    
    static String getTriggerNode(final String ip) {
        return String.format(TRIGGER, ip);
    }
    
    static String getDisabledNode(final String ip) {
        return String.format(DISABLED, ip);
    }
    
    static String getPausedNode(final String ip) {
        return String.format(PAUSED, ip);
    }
    
    static String getShutdownNode(final String ip) {
        return String.format(SHUTDOWN, ip);
    }
    
    /**
     * 判断给定路径是否为作业服务器立刻触发路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器立刻触发路径
     */
    public boolean isLocalJobTriggerPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerNode.TRIGGER, localHostService.getIp())));
    }
    
    /**
     * 判断给定路径是否为作业服务器暂停路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器暂停路径
     */
    public boolean isLocalJobPausedPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerNode.PAUSED, localHostService.getIp())));
    }
    
    /**
     * 判断给定路径是否为作业服务器关闭路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器关闭路径
     */
    public boolean isLocalJobShutdownPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerNode.SHUTDOWN, localHostService.getIp())));
    }
    
    /**
     * 判断给定路径是否为作业服务器禁用路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器禁用路径
     */
    public boolean isLocalServerDisabledPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerNode.DISABLED, localHostService.getIp())));
    }
    
    /**
     * 判断给定路径是否为作业服务器状态路径.
     * 
     * @param path 待判断的路径
     * @return 是否为作业服务器状态路径
     */
    public boolean isServerStatusPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ServerNode.ROOT)) && path.endsWith(ServerNode.STATUS_APPENDIX);
    }
    
    /**
     * 判断给定路径是否为作业服务器禁用路径.
     * 
     * @param path 待判断的路径
     * @return 是否为作业服务器禁用路径
     */
    public boolean isServerDisabledPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ServerNode.ROOT)) && path.endsWith(ServerNode.DISABLED_APPENDIX);
    }
    
    /**
     * 判断给定路径是否为作业服务器关闭路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器关闭路径
     */
    public boolean isServerShutdownPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(ServerNode.ROOT)) && path.endsWith(ServerNode.SHUTDOWN_APPENDIX);
    }
}
