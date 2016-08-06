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

package com.dangdang.ddframe.job.event.log;

import com.dangdang.ddframe.job.event.JobExecutionEvent;
import com.dangdang.ddframe.job.event.JobTraceEvent;
import com.dangdang.ddframe.job.event.JobEventListener;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * 运行痕迹事件日志监听器.
 *
 * @author zhangliang
 */
@Slf4j
public final class LogJobEventListener implements JobEventListener {
    
    private static final String DATE_PATTERN = "yyyy-MM-dd hh:mm:ss.SSSS";
    
    @Override
    public String getName() {
        return "log";
    }
    
    @Override
    public void listen(final JobTraceEvent traceEvent) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        String msg = "Elastic-Job tracing => jobName: '{}', hostname: '{}', message: '{}', failureCause: '{}', timestamp: '{}'";
        switch (traceEvent.getLevel()) {
            case TRACE:
                log.trace(msg, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getFailureCause(), format.format(traceEvent.getTimestamp()));
                break;
            case DEBUG:
                log.debug(msg, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getFailureCause(), format.format(traceEvent.getTimestamp()));
                break;
            case INFO:
                log.info(msg, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getFailureCause(), format.format(traceEvent.getTimestamp()));
                break;
            case WARN:
                log.info(msg, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getFailureCause(), format.format(traceEvent.getTimestamp()));
                break;
            case ERROR:
                log.error(msg, traceEvent.getJobName(), traceEvent.getHostname(), traceEvent.getMessage(), traceEvent.getFailureCause(), format.format(traceEvent.getTimestamp()));
                break;
            default:
                break;
        }
    }
    
    @Override
    public void listen(final JobExecutionEvent jobExecutionEvent) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        if (null == jobExecutionEvent.getCompleteTime()) {
            String msg = "Elastic-Job execution start => jobName: '{}', hostname: '{}', shardingItems: '{}', executionSource: '{}', startTime: '{}'";
            log.info(msg, jobExecutionEvent.getJobName(), jobExecutionEvent.getHostname(), jobExecutionEvent.getShardingItems(), jobExecutionEvent.getSource(),
                    format.format(jobExecutionEvent.getStartTime()));
            return;
        }
        if (jobExecutionEvent.isSuccess()) {
            String msg = "Elastic-Job execution success => jobName: '{}', hostname: '{}', shardingItems: '{}', executionSource: '{}', startTime: '{}', completeTime: '{}'";
            log.info(msg, jobExecutionEvent.getJobName(), jobExecutionEvent.getHostname(), jobExecutionEvent.getShardingItems(), jobExecutionEvent.getSource(),
                    format.format(jobExecutionEvent.getStartTime()), format.format(jobExecutionEvent.getCompleteTime()));
            return;
        }
        String msg = "Elastic-Job execution failure => jobName: '{}', hostname: '{}', shardingItems: '{}', executionSource: '{}', startTime: '{}', completeTime: '{}', failureCause: '{}'";
        log.error(msg, jobExecutionEvent.getJobName(), jobExecutionEvent.getHostname(), jobExecutionEvent.getShardingItems(), jobExecutionEvent.getSource(),
                format.format(jobExecutionEvent.getStartTime()), format.format(jobExecutionEvent.getCompleteTime()), jobExecutionEvent.getFailureCause());
    }
}
