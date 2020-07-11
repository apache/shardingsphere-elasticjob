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

package org.apache.shardingsphere.elasticjob.lite.lifecycle.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Job sharding info.
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
     * Job sharding status.
     */
    public enum ShardingStatus {
        
        DISABLED, 
        RUNNING,
        SHARDING_FLAG,
        PENDING;
    
        /**
         * Get sharding status.
         * 
         * @param isDisabled is disabled 
         * @param isRunning is running
         * @param isShardingFlag is need to Sharding
         * @return job sharding status
         */
        public static ShardingStatus getShardingStatus(final boolean isDisabled, final boolean isRunning, final boolean isShardingFlag) {
            if (isDisabled) {
                return DISABLED;
            }
            if (isRunning) {
                return RUNNING;
            }
            if (isShardingFlag) {
                return SHARDING_FLAG;
            }
            return PENDING;
        }
    }
}
