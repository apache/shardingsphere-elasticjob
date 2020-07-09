/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.elasticjob.cloud.event.type;

import org.apache.shardingsphere.elasticjob.cloud.exception.ExceptionUtil;
import org.apache.shardingsphere.elasticjob.cloud.event.JobEvent;
import org.apache.shardingsphere.elasticjob.cloud.util.env.IpUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * Job execution event.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public final class JobExecutionEvent implements JobEvent {
    
    private String id = UUID.randomUUID().toString();
    
    private String hostname = IpUtils.getHostName();
    
    private String ip = IpUtils.getIp();
    
    private final String taskId;
    
    private final String jobName;
    
    private final ExecutionSource source;
    
    private final int shardingItem;
    
    private Date startTime = new Date();
    
    @Setter
    private Date completeTime;
    
    @Setter
    private boolean success;
    
    @Setter
    private JobExecutionEventThrowable failureCause;

    /**
     * Execution success.
     *
     * @return job execution event
     */
    public JobExecutionEvent executionSuccess() {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, failureCause);
        result.setCompleteTime(new Date());
        result.setSuccess(true);
        return result;
    }

    /**
     * execution failure.
     *
     * @param failureCause failure cause
     * @return job execution event
     */
    public JobExecutionEvent executionFailure(final Throwable failureCause) {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, new JobExecutionEventThrowable(failureCause));
        result.setCompleteTime(new Date());
        result.setSuccess(false);
        return result;
    }

    /**
     * Get failure cause.
     *
     * @return failure cause
     */
    public String getFailureCause() {
        return ExceptionUtil.transform(failureCause == null ? null : failureCause.getThrowable());
    }

    /**
     * Execution source.
     */
    public enum ExecutionSource {
        NORMAL_TRIGGER, MISFIRE, FAILOVER
    }
}
