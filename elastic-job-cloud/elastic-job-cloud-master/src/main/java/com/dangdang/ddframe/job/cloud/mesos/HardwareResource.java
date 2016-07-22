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

package com.dangdang.ddframe.job.cloud.mesos;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.cloud.config.CloudJobConfiguration;
import com.dangdang.ddframe.job.cloud.context.JobContext;
import com.dangdang.ddframe.job.cloud.context.TaskContext;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.apache.mesos.Protos;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * 硬件资源.
 *
 * @author zhangliang
 */
@EqualsAndHashCode(of = "offerId")
public final class HardwareResource {
    
    private static final String RUN_COMMAND = "sh bin/start.sh '%s'";
    
    private final Protos.Offer offer;
    
    private final String offerId;
    
    private BigDecimal availableCpuCount;
    
    private BigDecimal availableMemoryMB;
    
    private BigDecimal reservedCpuCount;
    
    private BigDecimal reservedMemoryMB;
    
    public HardwareResource(final Protos.Offer offer) {
        this.offer = offer;
        offerId = offer.getId().getValue();
        availableCpuCount = getResource("cpus");
        availableMemoryMB = getResource("mem");
        reservedCpuCount = BigDecimal.ZERO;
        reservedMemoryMB = BigDecimal.ZERO;
    }
    
    private BigDecimal getResource(final String type) {
        for (Protos.Resource each : offer.getResourcesList()) {
            if (type.equals(each.getName())) {
                return new BigDecimal(Double.toString(each.getScalar().getValue()));
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * 根据需资源计算可以分多少片.
     * 
     * @param expectedShardingCount 期望的分片总数
     * @param perCpuCount 每片需要使用的CPU数量
     * @param perMemoryMB 每片需要使用的内存兆字节数量
     * @return 分片数量
     */
    public int calculateShardingCount(final int expectedShardingCount, final double perCpuCount, final double perMemoryMB) {
        int cpuShardingCount = availableCpuCount.divide(new BigDecimal(Double.toString(perCpuCount)), BigDecimal.ROUND_DOWN).intValue();
        if (cpuShardingCount > expectedShardingCount) {
            cpuShardingCount = expectedShardingCount;
        }
        int memoryShardingCount = availableMemoryMB.divide(new BigDecimal(Double.toString(perMemoryMB)), BigDecimal.ROUND_DOWN).intValue();
        if (memoryShardingCount > expectedShardingCount) {
            memoryShardingCount = expectedShardingCount;
        }
        return cpuShardingCount < memoryShardingCount ? cpuShardingCount : memoryShardingCount;
    }
    
    /**
     * 预留资源.
     * 
     * @param toBeReservedCpuCount 需预留的CPU数量
     * @param toBeReservedMemoryMB 需预留的内存兆字节数量
     */
    public void reserveResources(final double toBeReservedCpuCount, final double toBeReservedMemoryMB) {
        BigDecimal toBeReservedCpuCountDecimal = new BigDecimal(Double.toString(toBeReservedCpuCount));
        BigDecimal toBeReservedMemoryMBDecimal = new BigDecimal(Double.toString(toBeReservedMemoryMB));
        Preconditions.checkArgument(availableCpuCount.doubleValue() >= toBeReservedCpuCountDecimal.add(reservedCpuCount).doubleValue());
        Preconditions.checkArgument(availableMemoryMB.doubleValue() >= toBeReservedMemoryMBDecimal.add(reservedMemoryMB).doubleValue());
        reservedCpuCount = reservedCpuCount.add(toBeReservedCpuCountDecimal);
        reservedMemoryMB = reservedMemoryMB.add(toBeReservedMemoryMBDecimal);
    }
    
    /**
     * 提交预留资源.
     */
    public void commitReservedResources() {
        availableCpuCount = availableCpuCount.subtract(reservedCpuCount);
        availableMemoryMB = availableMemoryMB.subtract(reservedMemoryMB);
        reservedCpuCount = BigDecimal.ZERO;
        reservedMemoryMB = BigDecimal.ZERO;
    }
    
    /**
     * 创建Mesos任务对象.
     *
     * @param jobContext 云作业配置
     * @param shardingItem 分片项
     * @return 任务对象
     */
    public Protos.TaskInfo createTaskInfo(final JobContext jobContext, final int shardingItem) {
        CloudJobConfiguration jobConfig = jobContext.getJobConfig();
        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(new TaskContext(jobConfig.getJobName(), shardingItem, jobContext.getType(), offer.getSlaveId().getValue()).getId()).build();
        // TODO 完善offset和param
        ShardingContext shardingContext = new ShardingContext(jobContext.getJobConfig().getJobName(), jobContext.getJobConfig().getShardingTotalCount(), "", 10, 
                Collections.singletonList(new ShardingContext.ShardingItem(shardingItem, "", "")));
        // TODO 上线前更改cache为true
        Protos.CommandInfo.URI uri = Protos.CommandInfo.URI.newBuilder().setValue(jobConfig.getAppURL()).setExtract(true).setCache(false).build();
        Protos.CommandInfo command = Protos.CommandInfo.newBuilder().addUris(uri).setShell(true).setValue(String.format(RUN_COMMAND, GsonFactory.getGson().toJson(shardingContext))).build();
        return Protos.TaskInfo.newBuilder()
                .setName(taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .addResources(buildResource("cpus", jobConfig.getCpuCount()))
                .addResources(buildResource("mem", jobConfig.getMemoryMB()))
                .setCommand(command)
                .build();
    }
    
    private Protos.Resource.Builder buildResource(final String type, final double resourceValue) {
        return Protos.Resource.newBuilder().setName(type).setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(resourceValue));
    }
}
