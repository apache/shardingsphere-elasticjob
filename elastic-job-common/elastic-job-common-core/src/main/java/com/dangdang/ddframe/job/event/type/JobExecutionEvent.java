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

package com.dangdang.ddframe.job.event.type;

import com.dangdang.ddframe.job.event.JobEvent;
import com.dangdang.ddframe.job.exception.ExceptionUtil;
import com.dangdang.ddframe.job.util.env.LocalHostService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * 作业执行事件.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class JobExecutionEvent implements JobEvent {
    
    private static LocalHostService localHostService = new LocalHostService();
    
    private String id = UUID.randomUUID().toString();
    
    private String hostname = localHostService.getHostName();
    
    private String ip = localHostService.getIp();
    
    private final String taskId;
    
    private final String jobName;
    
    private final ExecutionSource source;
    
    private final int shardingItem;
    
    private Date startTime = new Date();
    
    private Date completeTime;
    
    private boolean success;
    
    private JobExecutionEventThrowable failureCause;
    
    /**
     * 作业执行成功.
     */
    public void executionSuccess() {
        completeTime = new Date();
        success = true;
    }
    
    /**
     * 作业执行失败.
     * 
     * @param failureCause 失败原因
     */
    public void executionFailure(final Throwable failureCause) {
        completeTime = new Date();
        success = false;
        this.failureCause = new JobExecutionEventThrowable(failureCause);
    }
    
    /**
     * 获取失败原因.
     * @return 失败原因
     */
    public String getFailureCause() {
        return ExceptionUtil.transform(failureCause == null ? null : failureCause.getThrowable());
    }
    
    /**
     * 执行来源.
     */
    public enum ExecutionSource {
        NORMAL_TRIGGER, MISFIRE, FAILOVER
    }
}
