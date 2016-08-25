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

import com.dangdang.ddframe.env.LocalHostService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 作业运行痕迹事件.
 *
 * @author zhangli
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class JobTraceEvent {
    
    private static LocalHostService localHostService = new LocalHostService();
    
    private final String jobName;
    
    private final LogLevel logLevel;
    
    private final String message;
    
    private Throwable failureCause;
    
    private final String hostname = localHostService.getHostName();
    
    private final Date creationTime = new Date();
    
    /**
     * 获取失败原因.
     * @return 失败原因
     */
    public String getFailureCause() {
        if (null == failureCause) {
            return "";
        }
        StringWriter result = new StringWriter();
        try (PrintWriter writer = new PrintWriter(result)) {
            failureCause.printStackTrace(writer);
        }
        return result.toString();
    }
    
    /**
     * 事件级别.
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
