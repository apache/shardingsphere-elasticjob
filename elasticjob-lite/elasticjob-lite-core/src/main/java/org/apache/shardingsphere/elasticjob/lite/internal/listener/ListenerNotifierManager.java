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

package org.apache.shardingsphere.elasticjob.lite.internal.listener;

import org.apache.curator.utils.ThreadUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Manage listener's notify executor,
 * each job has its own listener notify executor.
 */
public final class ListenerNotifierManager {

    private static volatile ListenerNotifierManager instance;

    private final Map<String, Executor> listenerNotifyExecutors = new ConcurrentHashMap<>();

    private ListenerNotifierManager() { }

    /**
     * Get singleton instance of ListenerNotifierManager.
     * @return singleton instance of ListenerNotifierManager.
     */
    public static ListenerNotifierManager getInstance() {
        if (null == instance) {
            synchronized (ListenerNotifierManager.class) {
                if (null == instance) {
                    instance = new ListenerNotifierManager();
                }
            }
        }
        return instance;
    }

    /**
     * Register a listener notify executor for the job specified.
     * @param jobName The job's name.
     */
    public void registerJobNotifyExecutor(final String jobName) {
        if (!listenerNotifyExecutors.containsKey(jobName)) {
            synchronized (this) {
                if (!listenerNotifyExecutors.containsKey(jobName)) {
                    ThreadFactory threadFactory = ThreadUtils.newGenericThreadFactory("ListenerNotify-" + jobName);
                    Executor notifyExecutor = Executors.newSingleThreadExecutor(threadFactory);
                    listenerNotifyExecutors.put(jobName, notifyExecutor);
                }
            }
        }
    }

    /**
     * Get the listener notify executor for the specified job.
     * @param jobName The job's name.
     * @return The job listener's notify executor.
     */
    public Executor getJobNotifyExecutor(final String jobName) {
        return listenerNotifyExecutors.get(jobName);
    }
}
