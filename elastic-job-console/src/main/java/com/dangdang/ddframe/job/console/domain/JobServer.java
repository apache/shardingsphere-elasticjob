/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.console.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import com.google.common.base.Strings;

@Getter
@Setter
public final class JobServer implements Serializable {
    
    private static final long serialVersionUID = 4241736510750597679L;
    
    private String jobName;
    
    private String ip;
    
    private String hostName;
    
    private ServerStatus status;
    
    private int processSuccessCount;
    
    private int processFailureCount;
    
    private String sharding;
    
    private boolean leader;
    
    private boolean leaderPaused;
    
    public enum ServerStatus {
        READY, 
        RUNNING, 
        DISABLED,
        PAUSED, 
        CRASHED, 
        SHUTDOWN;
        
        public static ServerStatus getServerStatus(final String status, final boolean disabled, final boolean paused, final boolean shutdown) {
            if (shutdown) {
                return ServerStatus.SHUTDOWN;
            }
            if (Strings.isNullOrEmpty(status)) {
                return ServerStatus.CRASHED;
            }
            if (disabled) {
                return ServerStatus.DISABLED;
            }
            if (paused) {
                return ServerStatus.PAUSED;
            }
            return ServerStatus.valueOf(status);
        }
    }
}
