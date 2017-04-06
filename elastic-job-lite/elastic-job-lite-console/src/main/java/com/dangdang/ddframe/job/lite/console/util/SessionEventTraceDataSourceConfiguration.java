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

package com.dangdang.ddframe.job.lite.console.util;

import com.dangdang.ddframe.job.lite.console.domain.EventTraceDataSourceConfiguration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 事件追踪数据源配置的会话声明周期.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionEventTraceDataSourceConfiguration {
    
    private static EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration;
    
    /**
     * 从当前会话范围获取事件追踪数据源配置.
     * 
     * @return 事件追踪数据源配置
     */
    public static EventTraceDataSourceConfiguration getEventTraceDataSourceConfiguration() {
        return eventTraceDataSourceConfiguration;
    }
    
    /**
     * 设置事件追踪数据源配置至当前会话范围.
     *
     * @param eventTraceDataSourceConfiguration 事件追踪数据源配置
     */
    public static void setDataSourceConfiguration(final EventTraceDataSourceConfiguration eventTraceDataSourceConfiguration) {
        SessionEventTraceDataSourceConfiguration.eventTraceDataSourceConfiguration = eventTraceDataSourceConfiguration;
    }
}
