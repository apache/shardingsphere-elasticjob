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

package com.dangdang.ddframe.job.util.trace.log;

import com.dangdang.ddframe.job.util.trace.TraceEvent;
import com.dangdang.ddframe.job.util.trace.TraceEventListener;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * 运行痕迹事件日志监听器.
 *
 * @author zhangliang
 */
@Slf4j
public final class LogTraceEventListener implements TraceEventListener {
    
    @Override
    public String getName() {
        return "log";
    }
    
    @Override
    public void listen(final TraceEvent traceEvent) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSS");
        String logMessage = "Elastic-Job => jobName: '{}', hostname: '{}', message: '{}', error_details: '{}', timestamp: {}";
        switch (traceEvent.getLevel()) {
            case TRACE:
                // TODO traceEvent.getCause() 要打印出stacktrace
                log.trace(logMessage, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getCause(), format.format(traceEvent.getTimestamp()));
                break;
            case DEBUG:
                log.debug(logMessage, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getCause(), format.format(traceEvent.getTimestamp()));
                break;
            case INFO:
                log.info(logMessage, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getCause(), format.format(traceEvent.getTimestamp()));
                break;
            case WARN:
                log.info(logMessage, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getCause(), format.format(traceEvent.getTimestamp()));
                break;
            case ERROR:
                log.error(logMessage, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getCause(), format.format(traceEvent.getTimestamp()));
                break;
            default:
                break;
        }
    }
}
