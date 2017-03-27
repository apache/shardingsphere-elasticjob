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

import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
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
    
    static final String STATUS_APPENDIX = "status";
    
    static final String STATUS = ROOT + "/%s/%s/" + STATUS_APPENDIX;
    
    static final String TRIGGER_APPENDIX = "trigger";
    
    static final String TRIGGER = ROOT + "/%s/%s/" + TRIGGER_APPENDIX;
    
    private final String jobName;
    
    private final String ip;
    
    private final JobNodePath jobNodePath;
    
    public ServerNode(final String jobName) {
        this.jobName = jobName;
        ip = new LocalHostService().getIp();
        jobNodePath = new JobNodePath(jobName);
    }
    
    String getStatusNode() {
        return String.format(STATUS, ip, JobRegistry.getInstance().getJobInstanceId(jobName));
    }
    
    static String getStatusNode(final String ip, final String jobInstanceId) {
        return String.format(STATUS, ip, jobInstanceId);
    }
    
    String getTriggerNode(final String ip) {
        return String.format(TRIGGER, ip, JobRegistry.getInstance().getJobInstanceId(jobName));
    }
    
    /**
     * 判断给定路径是否为作业服务器立刻触发路径.
     *
     * @param path 待判断的路径
     * @return 是否为作业服务器立刻触发路径
     */
    public boolean isLocalJobTriggerPath(final String path) {
        return path.startsWith(jobNodePath.getFullPath(String.format(ServerNode.TRIGGER, ip, JobRegistry.getInstance().getJobInstanceId(jobName))));
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
}
