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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.cloud.job.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.task.ElasticJobTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.CommandInfo.URI;

import java.math.BigDecimal;

/**
 * Mesos资源工具类.
 *
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MesosUtil {
    
    /**
     * 创建Mesos任务对象.
     * 
     * @param offer 资源提供对象
     * @param cloudJobConfig 云作业配置
     * @param shardingItem 分片项
     * @return Mesos任务对象
     */
    public static Protos.TaskInfo createTaskInfo(final Protos.Offer offer, final CloudJobConfiguration cloudJobConfig, final int shardingItem) {
        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(new ElasticJobTask(cloudJobConfig.getJobName(), shardingItem).getId()).build();
        URI uri = Protos.CommandInfo.URI.newBuilder().setValue(cloudJobConfig.getAppURL()).setExtract(true).setCache(true).build();
        Protos.CommandInfo command = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true).setValue("sh bin/start.sh " + taskId.getValue()).build();
        Protos.ExecutorInfo executorInfo = Protos.ExecutorInfo.newBuilder().setExecutorId(Protos.ExecutorID.newBuilder().setValue(taskId.getValue())).setCommand(command).build();
        return Protos.TaskInfo.newBuilder()
                .setName(taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(getResources("cpus", cloudJobConfig.getCpuCount()))
                .addResources(getResources("mem", cloudJobConfig.getMemoryMB()))
                .setExecutor(executorInfo)
                .build();
    }
    
    private static Protos.Resource.Builder getResources(final String type, final double value) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(value));
    }
    
    /**
     * 获取资源值.
     * 
     * @param offer 资源提供对象
     * @param type 资源类型
     * @return 资源值
     */
    public static BigDecimal getValue(final Protos.Offer offer, final String type) {
        for (Protos.Resource each : offer.getResourcesList()) {
            if (type.equals(each.getName())) {
                return new BigDecimal(each.getScalar().getValue());
            }
        }
        return BigDecimal.ZERO;
    }
}
