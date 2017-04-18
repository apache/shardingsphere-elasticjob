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

package com.dangdang.ddframe.job.lite.lifecycle.internal.operate;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodePath;
import com.dangdang.ddframe.job.lite.lifecycle.api.ShardingOperateAPI;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;

/**
 * 操作分片的实现类.
 *
 * @author caohao
 */
public final class ShardingOperateAPIImpl implements ShardingOperateAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    public ShardingOperateAPIImpl(final CoordinatorRegistryCenter regCenter) {
        this.regCenter = regCenter;
    }
    
    @Override
    public void disable(final String jobName, final String item) {
        disableOrEnableJobs(jobName, item, true);
    }
    
    @Override
    public void enable(final String jobName, final String item) {
        disableOrEnableJobs(jobName, item, false);
    }
    
    private void disableOrEnableJobs(final String jobName, final String item, final boolean disabled) {
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String shardingDisabledNodePath = jobNodePath.getShardingNodePath(item, "disabled");
        if (disabled) {
            regCenter.persist(shardingDisabledNodePath, "");
        } else {
            regCenter.remove(shardingDisabledNodePath);
        }
    }
}
