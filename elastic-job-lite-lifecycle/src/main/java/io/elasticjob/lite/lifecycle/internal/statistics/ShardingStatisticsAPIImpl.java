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

package io.elasticjob.lite.lifecycle.internal.statistics;

import io.elasticjob.lite.internal.storage.JobNodePath;
import io.elasticjob.lite.lifecycle.api.ShardingStatisticsAPI;
import io.elasticjob.lite.lifecycle.domain.ShardingInfo;
import io.elasticjob.lite.reg.base.CoordinatorRegistryCenter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 作业分片状态展示的实现类.
 *
 * @author caohao
 */
@RequiredArgsConstructor
public final class ShardingStatisticsAPIImpl implements ShardingStatisticsAPI {
    
    private final CoordinatorRegistryCenter regCenter;
    
    @Override
    public Collection<ShardingInfo> getShardingInfo(final String jobName) {
        String shardingRootPath = new JobNodePath(jobName).getShardingNodePath();
        List<String> items = regCenter.getChildrenKeys(shardingRootPath);
        List<ShardingInfo> result = new ArrayList<>(items.size());
        for (String each : items) {
            result.add(getShardingInfo(jobName, each));
        }
        Collections.sort(result);
        return result;
    }
    
    private ShardingInfo getShardingInfo(final String jobName, final String item) {
        ShardingInfo result = new ShardingInfo();
        result.setItem(Integer.parseInt(item));
        JobNodePath jobNodePath = new JobNodePath(jobName);
        String instanceId = regCenter.get(jobNodePath.getShardingNodePath(item, "instance"));
        boolean disabled = regCenter.isExisted(jobNodePath.getShardingNodePath(item, "disabled"));
        boolean running = regCenter.isExisted(jobNodePath.getShardingNodePath(item, "running"));
        boolean shardingError = !regCenter.isExisted(jobNodePath.getInstanceNodePath(instanceId));
        result.setStatus(ShardingInfo.ShardingStatus.getShardingStatus(disabled, running, shardingError));
        result.setFailover(regCenter.isExisted(jobNodePath.getShardingNodePath(item, "failover")));
        if (null != instanceId) {
            String[] ipAndPid = instanceId.split("@-@");
            result.setServerIp(ipAndPid[0]);
            result.setInstanceId(ipAndPid[1]);
        }
        return result;
    }
}
