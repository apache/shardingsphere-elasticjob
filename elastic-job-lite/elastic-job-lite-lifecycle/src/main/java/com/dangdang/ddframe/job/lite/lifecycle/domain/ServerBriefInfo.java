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

package com.dangdang.ddframe.job.lite.lifecycle.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;

/**
 * 作业服务器简明信息对象.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class ServerBriefInfo implements Serializable, Comparable<ServerBriefInfo> {
    
    private static final long serialVersionUID = 1133149706443681483L;
    
    private String serverIp;
    
    private String serverHostName;
    
    private ServerBriefStatus status;
    
    @Override
    public int compareTo(final ServerBriefInfo o) {
        return getServerIp().compareTo(o.getServerIp());
    }
    
    /**
     * 作业服务器状态.
     *
     * @author zhangliang
     */
    public enum ServerBriefStatus {
        
        OK, 
        PARTIAL_ALIVE, 
        ALL_CRASHED;
        
        /**
         * 获取作业服务器状态.
         * 
         * @param aliveServers 存活的作业服务器集合
         * @param crashedServers 崩溃的作业服务器集合
         * @param serverIp 作业服务器IP地址
         * @return 作业服务器状态
         */
        public static ServerBriefStatus getServerBriefStatus(final Collection<String> aliveServers, final Collection<String> crashedServers, final String serverIp) {
            if (!aliveServers.contains(serverIp)) {
                return ALL_CRASHED;
            }
            if (!crashedServers.contains(serverIp)) {
                return OK;
            }
            return PARTIAL_ALIVE;
        }
    }
}
