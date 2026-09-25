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

package org.apache.shardingsphere.elasticjob.kernel.infra.util;

import java.util.concurrent.TimeUnit;

/**
 * Tracks elapsed time and interruption for a bounded wait loop, so callers can consistently
 * decide when to give up instead of blocking indefinitely.
 */
public final class BoundedWaitGuard {
    
    private final long maxWaitNanos;
    
    private final long startNanos;
    
    private BoundedWaitGuard(final long maxWaitMillis) {
        maxWaitNanos = TimeUnit.MILLISECONDS.toNanos(maxWaitMillis);
        startNanos = System.nanoTime();
    }
    
    /**
     * Start tracking a new bounded wait.
     *
     * @param maxWaitMillis max milliseconds to wait before giving up
     * @return a new guard, timing starts now
     */
    public static BoundedWaitGuard start(final long maxWaitMillis) {
        return new BoundedWaitGuard(maxWaitMillis);
    }
    
    /**
     * Judge whether the caller should give up waiting: either the thread was interrupted,
     * or the configured max wait time has elapsed.
     *
     * @return whether to give up
     */
    public boolean shouldGiveUp() {
        return Thread.currentThread().isInterrupted() || System.nanoTime() - startNanos > maxWaitNanos;
    }
    
    /**
     * Judge whether giving up is specifically due to interruption, so callers can log accordingly.
     *
     * @return whether the current thread is interrupted
     */
    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }
}
