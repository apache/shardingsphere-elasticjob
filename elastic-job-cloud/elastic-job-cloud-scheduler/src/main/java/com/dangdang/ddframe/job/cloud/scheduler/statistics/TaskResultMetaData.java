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

package com.dangdang.ddframe.job.cloud.scheduler.statistics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统计元数据.
 *
 * @author liguangyun
 */
public final class TaskResultMetaData {
    
    private final AtomicInteger successCount;
    
    private final AtomicInteger failedCount;
    
    /**
     * 构造函数.
     */
    public TaskResultMetaData() {
        successCount = new AtomicInteger(0);
        failedCount = new AtomicInteger(0);
    }
    
    /**
     * 增加并获取成功数.
     * 
     * @return 成功数
     */
    public int incrementAndGetSuccessCount() {
        return successCount.incrementAndGet();
    }
    
    /**
     * 增加并获取失败数.
     * 
     * @return 失败数
     */
    public int incrementAndGetFailedCount() {
        return failedCount.incrementAndGet();
    }
    
    /**
     * 获取成功数.
     * 
     * @return 成功数
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * 获取失败数.
     * 
     * @return 失败数
     */
    public int getFailedCount() {
        return failedCount.get();
    }
    
    /**
     * 重置成功数、失败数.
     */
    public void reset() {
        successCount.set(0);
        failedCount.set(0);
    }
}
