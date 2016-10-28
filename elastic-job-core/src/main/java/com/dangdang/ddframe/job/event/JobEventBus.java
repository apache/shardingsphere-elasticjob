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

import com.dangdang.ddframe.job.util.concurrent.ExecutorServiceObject;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    
    /**
     * 获取运行痕迹事件总线单例.
     * 
     * @return 运行痕迹事件总线单例
     */
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
    public void register(final String jobName, final Collection<JobEventConfiguration> jobEventConfigs) {
        JobEventBusInstance newValue = new JobEventBusInstance();
        JobEventBusInstance originalValue = itemMap.putIfAbsent(jobName, newValue);
        if (null != originalValue) {
            originalValue.register(jobEventConfigs);
        } else {
            newValue.register(jobEventConfigs);
        }
    }
    
    /**
     * 发布事件.
     *
     * @param jobEvent 作业事件
     */
    public void post(final JobEvent jobEvent) {
        JobEventBusInstance jobEventBusInstance = itemMap.get(jobEvent.getJobName());
        if (null != jobEventBusInstance) {
            jobEventBusInstance.post(jobEvent);
        }
    }
    
    /**
     * 清除监听器.
     * 
     * @param jobName 作业名
     */
    public void clearListeners(final String jobName) {
        JobEventBusInstance jobEventBusInstance = itemMap.get(jobName);
        if (null != jobEventBusInstance) {
            jobEventBusInstance.clearListeners();
        }
    }
    
    // TODO 通过JMX暴露
    public Map<String, Integer> getWorkQueueSize() {
        Map<String, Integer> result = new HashMap<>();
        for (Entry<String, JobEventBusInstance> each : itemMap.entrySet()) {
            result.put(each.getKey(), each.getValue().getExecutorServiceObject().getWorkQueueSize());
        }
        return result;
    }
    
    // TODO 通过JMX暴露
    public Map<String, Integer> getActiveThreadCount() {
        Map<String, Integer> result = new HashMap<>();
        for (Entry<String, JobEventBusInstance> each : itemMap.entrySet()) {
            result.put(each.getKey(), each.getValue().getExecutorServiceObject().getActiveThreadCount());
        }
        return result;
    }
    
    private class JobEventBusInstance {
        
        @Getter
        private final ExecutorServiceObject executorServiceObject;
        
        private final EventBus eventBus;
        
        private final ConcurrentHashMap<String, JobEventListener> listeners = new ConcurrentHashMap<>();
        
        JobEventBusInstance() {
            executorServiceObject = new ExecutorServiceObject(Runtime.getRuntime().availableProcessors() * 2);
            eventBus = new AsyncEventBus(executorServiceObject.createExecutorService());
        }
        
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
        
        synchronized void clearListeners() {
            for (Object each : listeners.values()) {
                eventBus.unregister(each);
            }
            listeners.clear();
        }
    }
}
