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

package com.dangdang.ddframe.job.internal.listener;

import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import com.dangdang.ddframe.job.api.config.JobConfiguration;
import com.dangdang.ddframe.job.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;

/**
 * 作业注册中心的监听器管理者的抽象类.
 * 
 * @author zhangliang
 */
public abstract class AbstractListenerManager {
    
    private final JobNodeStorage jobNodeStorage;
    
    protected AbstractListenerManager(final CoordinatorRegistryCenter coordinatorRegistryCenter, final JobConfiguration jobConfiguration) {
        jobNodeStorage = new JobNodeStorage(coordinatorRegistryCenter, jobConfiguration);
    }

    /**
     * 开启监听器.
     */
    public abstract void start();
    
    protected void addDataListener(final TreeCacheListener listener) {
        jobNodeStorage.addDataListener(listener);
    }
    
    protected void addConnectionStateListener(final ConnectionStateListener listener) {
        jobNodeStorage.addConnectionStateListener(listener);
    }
}
