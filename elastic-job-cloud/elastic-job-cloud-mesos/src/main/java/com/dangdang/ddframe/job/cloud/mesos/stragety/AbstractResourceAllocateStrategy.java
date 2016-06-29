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
import com.dangdang.ddframe.job.cloud.Internal.task.CloudJobTaskService;
import lombok.Getter;
import org.apache.mesos.Protos;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资源分配策略抽象类.
 *
 * @author zhangliang
 */
public abstract class AbstractResourceAllocateStrategy implements ResourceAllocateStrategy {
    
    @Getter
    private final CloudJobTaskService taskService = new CloudJobTaskService();
    
    protected BigDecimal getValue(final List<Protos.Resource> resources, final String type) {
        for (Protos.Resource each : resources) {
            if (type.equals(each.getName())) {
                return new BigDecimal(each.getScalar().getValue());
            }
        }
        return BigDecimal.ZERO;
    }
    
    protected Protos.Resource.Builder getResources(final String type, final double value) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(value));
    }
    
    protected Protos.TaskInfo createTaskInfo(final Protos.Offer offer, final CloudJobConfiguration cloudJobConfig, final int shardingItem) {
        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(getTaskService().generateTaskId(cloudJobConfig.getJobName(), shardingItem)).build();
        return Protos.TaskInfo.newBuilder()
                .setName(taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(getResources("cpus", cloudJobConfig.getCpuCount()))
                .addResources(getResources("mem", cloudJobConfig.getMemoryMB()))
                .setCommand(Protos.CommandInfo.newBuilder().setValue("/Users/zhangliang/docker-sample/elastic-job-example/bin/start.sh " + taskId.getValue() + " > /Users/zhangliang/docker-sample/elastic-job-example/logs/log" + taskService.getJobTask(taskId.getValue()).getShardingItem() + ".log"))
                .build();
    }
}
