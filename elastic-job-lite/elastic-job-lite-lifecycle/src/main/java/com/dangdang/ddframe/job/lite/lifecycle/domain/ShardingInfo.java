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

/**
 * 作业分片信息对象.
 *
 * @author caohao
 */
@Getter
@Setter
public final class ShardingInfo implements Serializable, Comparable<ShardingInfo> {
    
    private static final long serialVersionUID = 8587397581949456718L;
    
    private int item;
    
    private String serverIp;

    private String instanceId;

    private ShardingStatus status;
    
    private boolean failover;
    
    @Override
    public int compareTo(final ShardingInfo o) {
        return getItem() - o.getItem();
    }
    
    /**
     * 作业分片状态.
     *
     * @author caohao
     */
    public enum ShardingStatus {
        
        DISABLED, 
        RUNNING, 
        SHARDING_ERROR,
        PENDING;
    
        /**
         * 获取分片状态.
         * 
         * @param isDisabled 是否被禁用 
         * @param isRunning 是否在运行
         * @param isShardingError 是否分片错误
         * @return 作业运行时状态
         */
        public static ShardingStatus getShardingStatus(final boolean isDisabled, final boolean isRunning, final boolean isShardingError) {
            if (isDisabled) {
                return DISABLED;
            }
            if (isRunning) {
                return RUNNING;
            }
            if (isShardingError) {
                return SHARDING_ERROR;
            }
            return PENDING;
        }
    }
}
