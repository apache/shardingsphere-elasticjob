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

import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.JobNodeStorage;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;

/**
 * Listener manager.
 */
public abstract class AbstractListenerManager {
    
    private final JobNodeStorage jobNodeStorage;
    
    protected AbstractListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
    }
    
    /**
     * Start listener.
     */
    public abstract void start();
    
    protected void addDataListener(final CuratorCacheListener listener) {
        jobNodeStorage.addDataListener(listener);
    }
}
