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

package org.apache.shardingsphere.elasticjob.lite.internal.util;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread utility.
 */
@Slf4j
public final class ThreadUtils {

    private ThreadUtils() {

    }

    /**
     * Create a new generic thread factory instance.
     *
     * @param processName Process thread name prefix.
     * @return Return generic thread factory instance.
     */
    public static ThreadFactory newGenericThreadFactory(final String processName) {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (t, e) -> {
            log.error("Unexpected exception in thread: " + t, e);
            Throwables.throwIfUnchecked(e);
        };
        return new ThreadFactoryBuilder()
            .setNameFormat(processName + "-%d")
            .setDaemon(true)
            .setUncaughtExceptionHandler(uncaughtExceptionHandler)
            .build();
    }
}
