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

import com.dangdang.ddframe.job.cloud.mesos.MesosUtil;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.mesos.Protos;

import java.math.BigDecimal;

/**
 * 机器资源.
 *
 * @author zhangliang
 */
@EqualsAndHashCode(of = "offerId")
public final class MachineResource {
    
    @Getter
    private final Protos.Offer offer;
    
    private final String offerId;
    
    private BigDecimal availableCpuCount;
    
    private BigDecimal availableMemoryMB;
    
    private BigDecimal reservedCpuCount;
    
    private BigDecimal reservedMemoryMB;
    
    public MachineResource(final Protos.Offer offer) {
        this.offer = offer;
        offerId = offer.getId().getValue();
        availableCpuCount = MesosUtil.getValue(offer, "cpus");
        availableMemoryMB = MesosUtil.getValue(offer, "mem");
        reservedCpuCount = BigDecimal.ZERO;
        reservedMemoryMB = BigDecimal.ZERO;
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
        int cpuShardingCount = availableCpuCount.divide(new BigDecimal(perCpuCount), BigDecimal.ROUND_DOWN).intValue();
        if (cpuShardingCount > expectedShardingCount) {
            cpuShardingCount = expectedShardingCount;
        }
        int memoryShardingCount = availableMemoryMB.divide(new BigDecimal(perMemoryMB), BigDecimal.ROUND_DOWN).intValue();
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
        BigDecimal toBeReservedCpuCountDecimal = new BigDecimal(toBeReservedCpuCount);
        BigDecimal toBeReservedMemoryMBDecimal = new BigDecimal(toBeReservedMemoryMB);
        Preconditions.checkArgument(availableCpuCount.doubleValue() >= toBeReservedCpuCountDecimal.add(reservedCpuCount).doubleValue());
        Preconditions.checkArgument(availableMemoryMB.doubleValue() >= toBeReservedMemoryMBDecimal.add(reservedMemoryMB).doubleValue());
        reservedCpuCount = reservedCpuCount.add(toBeReservedCpuCountDecimal);
        reservedMemoryMB = reservedMemoryMB.add(toBeReservedMemoryMBDecimal);
    }
    
    /**
     * 提交预留资源.
     */
    public void commitReservedResources() {
        availableCpuCount.subtract(reservedCpuCount);
        availableMemoryMB.subtract(reservedMemoryMB);
    }
}
