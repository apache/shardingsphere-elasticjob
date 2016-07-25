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

package com.dangdang.ddframe.job.api.dataflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统计处理数据数量的类.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessCountStatistics {
    
    private static ConcurrentMap<String, AtomicInteger> processSuccessCount = new ConcurrentHashMap<>();
    
    private static ConcurrentMap<String, AtomicInteger> processFailureCount = new ConcurrentHashMap<>();
    
    /**
     * 增加本作业服务器处理数据正确的数量.
     * 
     * @param jobName 作业名称
     */
    public static void incrementProcessSuccessCount(final String jobName) {
        incrementProcessCount(jobName, processSuccessCount);
    }
    
    /**
     * 增加本作业服务器处理数据错误的数量.
     * 
     * @param jobName 作业名称
     */
    public static void incrementProcessFailureCount(final String jobName) {
        incrementProcessCount(jobName, processFailureCount);
    }
    
    
    private static void incrementProcessCount(final String jobName, final ConcurrentMap<String, AtomicInteger> processCountMap) {
        processCountMap.putIfAbsent(jobName, new AtomicInteger(0));
        processCountMap.get(jobName).incrementAndGet();
    }
    
    /**
     * 获取本作业服务器处理数据正确的数量.
     * 
     * @param jobName 作业名称
     * @return 本作业服务器处理数据正确的数量
     */
    public static int getProcessSuccessCount(final String jobName) {
        return null == processSuccessCount.get(jobName) ? 0 : processSuccessCount.get(jobName).get();
    }
    
    /**
     * 获取本作业服务器处理数据错误的数量.
     * 
     * @param jobName 作业名称
     * @return 本作业服务器处理数据错误的数量
     */
    public static int getProcessFailureCount(final String jobName) {
        return null == processFailureCount.get(jobName) ? 0 : processFailureCount.get(jobName).get();
    }
    
    /**
     * 重置统计信息.
     * 
     * @param jobName 作业名称
     */
    public static void reset(final String jobName) {
        if (processSuccessCount.containsKey(jobName)) {
            processSuccessCount.get(jobName).set(0);
        }
        if (processFailureCount.containsKey(jobName)) {
            processFailureCount.get(jobName).set(0);
        }
    }
}
