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

package com.dangdang.ddframe.job.cloud.mesos.stragety;

import com.dangdang.ddframe.job.cloud.Internal.config.CloudJobConfiguration;
import org.apache.mesos.Protos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 同一机器资源优先策略.
 *
 * @author zhangliang
 */
public final class ExhaustFirstResourceAllocateStrategy extends AbstractResourceAllocateStrategy {
    
    @Override
    public List<Protos.TaskInfo> allocate(final List<Protos.Offer> offers, final CloudJobConfiguration cloudJobConfig) {
        List<Protos.TaskInfo> result = new ArrayList<>(cloudJobConfig.getShardingTotalCount());
        int startShardingItem = 0;
        int assignedShares = 0;
        for (Protos.Offer each : offers) {
            int availableCpuShare = getValue(each.getResourcesList(), "cpus").divide(new BigDecimal(cloudJobConfig.getCpuCount()), BigDecimal.ROUND_DOWN).intValue();
            int availableMemoriesShare = getValue(each.getResourcesList(), "mem").divide(new BigDecimal(cloudJobConfig.getMemoryMB()), BigDecimal.ROUND_DOWN).intValue();
            assignedShares += availableCpuShare < availableMemoriesShare ? availableCpuShare : availableMemoriesShare;
            for (int i = startShardingItem; i < assignedShares; i++) {
                result.add(createTaskInfo(each, cloudJobConfig, i));
                if (result.size() == cloudJobConfig.getShardingTotalCount()) {
                    return result;
                }
            }
            startShardingItem = assignedShares;
        }
        return result;
    }
}
