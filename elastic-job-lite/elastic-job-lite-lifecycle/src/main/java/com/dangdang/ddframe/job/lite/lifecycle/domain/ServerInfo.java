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

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 作业服务器对象.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class ServerInfo implements Serializable {
    
    private static final long serialVersionUID = 4241736510750597679L;
    
    private String jobName;
    
    private String ip;
    
    private String hostName;
    
    private ServerStatus status;
    
    private String sharding;
    
    /**
     * 作业服务器状态.
     *
     * @author zhangliang
     */
    public enum ServerStatus {
        
        READY, 
        RUNNING, 
        DISABLED,
        PAUSED, 
        CRASHED, 
        SHUTDOWN;
    
        /**
         * 获取作业服务器状态.
         * 
         * @param status 作业状态
         * @param isDisabled 作业是否禁用
         * @param isPaused 作业是否暂停
         * @param isShutdown 作业是否关闭
         * @return 作业服务器状态
         */
        public static ServerStatus getServerStatus(final String status, final boolean isDisabled, final boolean isPaused, final boolean isShutdown) {
            if (isShutdown) {
                return SHUTDOWN;
            }
            if (Strings.isNullOrEmpty(status)) {
                return CRASHED;
            }
            if (isDisabled) {
                return DISABLED;
            }
            if (isPaused) {
                return PAUSED;
            }
            return ServerStatus.valueOf(status);
        }
    }
}
