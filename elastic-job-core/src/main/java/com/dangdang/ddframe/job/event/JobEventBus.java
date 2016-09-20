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

package com.dangdang.ddframe.job.event;

import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行痕迹事件总线.
 * 
 * @author zhangliang
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobEventBus {
    
    private static volatile JobEventBus instance;
    
    private final ConcurrentHashMap<String, JobEventBusInstance> itemMap = new ConcurrentHashMap<>();
    
    public static JobEventBus getInstance() {
        if (null == instance) {
            synchronized (JobEventBus.class) {
                if (null == instance) {
                    instance = new JobEventBus();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册事件监听器.
     *
     * @param jobName 作业名
     * @param jobEventConfigs 作业事件配置
     */
    public synchronized void register(final String jobName, final Collection<JobEventConfiguration> jobEventConfigs) {
        itemMap.putIfAbsent(jobName, new JobEventBusInstance());
        itemMap.get(jobName).register(jobEventConfigs);
    }
    
    /**
     * 发布事件.
     *
     * @param jobEvent 作业事件
     */
    public synchronized void post(final JobEvent jobEvent) {
        String jobName = jobEvent.getJobName();
        if (itemMap.containsKey(jobName)) {
            itemMap.get(jobName).post(jobEvent);
        }
    }
    
    /**
     * 清除监听器.
     * 
     * @param jobName 作业名
     */
    public synchronized void clearListeners(final String jobName) {
        if (itemMap.containsKey(jobName)) {
            itemMap.get(jobName).clearListeners();
        }
    }
    
    @RequiredArgsConstructor
    private class JobEventBusInstance {
        
        private final EventBus eventBus = new EventBus();
        
        private final ConcurrentHashMap<String, JobEventListener> listeners = new ConcurrentHashMap<>();
        
        void register(final Collection<JobEventConfiguration> jobEventConfigs) {
            for (JobEventConfiguration each : jobEventConfigs) {
                register(each.createJobEventListener());
            }
        }
        
        private void register(final JobEventListener listener) {
            if (null != listener && null == listeners.putIfAbsent(listener.getIdentity(), listener)) {
                eventBus.register(listener);
            }
        }
        
        void post(final Object event) {
            if (!listeners.isEmpty()) {
                eventBus.post(event);
            }
        }
        
        void clearListeners() {
            for (Object each : listeners.values()) {
                eventBus.unregister(each);
            }
            listeners.clear();
        }
    }
}
