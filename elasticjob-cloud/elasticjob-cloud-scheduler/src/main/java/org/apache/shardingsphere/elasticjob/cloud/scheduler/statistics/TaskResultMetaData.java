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

package org.apache.shardingsphere.elasticjob.cloud.scheduler.statistics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task result meta data.
 */
public final class TaskResultMetaData {
    
    private final AtomicInteger successCount;
    
    private final AtomicInteger failedCount;

    public TaskResultMetaData() {
        successCount = new AtomicInteger(0);
        failedCount = new AtomicInteger(0);
    }
    
    /**
     * Increase and get success count.
     * 
     * @return success count
     */
    public int incrementAndGetSuccessCount() {
        return successCount.incrementAndGet();
    }
    
    /**
     * Increase and get failed count.
     * 
     * @return failed count
     */
    public int incrementAndGetFailedCount() {
        return failedCount.incrementAndGet();
    }
    
    /**
     * Get success count.
     * 
     * @return success count
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * Get failed count.
     * 
     * @return failed count
     */
    public int getFailedCount() {
        return failedCount.get();
    }
    
    /**
     * Reset success and failed count.
     */
    public void reset() {
        successCount.set(0);
        failedCount.set(0);
    }
}
