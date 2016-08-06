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

import com.dangdang.ddframe.job.util.env.LocalHostService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 运行痕迹事件.
 *
 * @author zhangli
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class TraceEvent {
    
    private static LocalHostService localHostService = new LocalHostService();
    
    private final String jobName;
    
    private final Level level;
    
    private final String message;
    
    private Throwable cause;
    
    private final String hostname = localHostService.getHostName();
    
    private final Date timestamp = new Date();
    
    /**
     * 获取stack trace字符串.
     * @return stack trace字符串
     */
    public String getCause() {
        if (null == cause) {
            return "";
        }
        StringWriter result = new StringWriter();
        try (PrintWriter writer = new PrintWriter(result)) {
            cause.printStackTrace(writer);
        }
        return result.toString();
    }
    
    /**
     * 事件级别.
     */
    public enum Level {
        
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
