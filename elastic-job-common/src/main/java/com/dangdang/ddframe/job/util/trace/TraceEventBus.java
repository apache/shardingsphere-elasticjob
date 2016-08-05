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

package com.dangdang.ddframe.job.util.trace;

import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 运行痕迹事件总线.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TraceEventBus {
    
    @Getter
    private static TraceEventBus instance = new TraceEventBus();
    
    private final EventBus eventBus = new EventBus();
    
    private final ConcurrentHashMap<TraceEventType, Object> listeners = new ConcurrentHashMap<>(TraceEventType.values().length, 1);
    
    /**
     * 注册事件监听器.
     *
     * @param listener 监听器
     */
    public void register(final TraceEventListener listener) {
        if (null == listeners.putIfAbsent(listener.getType(), listener)) {
            eventBus.register(listener);
        }
    }
    
    /**
     * 发布事件.
     *
     * @param traceEvent 事件
     */
    public void post(final TraceEvent traceEvent) {
        if (!listeners.isEmpty()) {
            eventBus.post(traceEvent);
        }
    }
    
    /**
     * 清除监听器.
     */
    public synchronized void clearListeners() {
        for (Object each : listeners.values()) {
            eventBus.unregister(each);
        }
        listeners.clear();
    }
}
