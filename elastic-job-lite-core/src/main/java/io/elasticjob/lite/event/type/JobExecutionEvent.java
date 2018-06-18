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

package io.elasticjob.lite.event.type;

import io.elasticjob.lite.event.JobEvent;
import io.elasticjob.lite.exception.ExceptionUtil;
import io.elasticjob.lite.util.env.IpUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
     * 作业执行成功.
     * 
     * @return 作业执行事件
     */
    public JobExecutionEvent executionSuccess() {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, failureCause);
        result.setCompleteTime(new Date());
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 作业执行失败.
     * 
     * @param failureCause 失败原因
     * @return 作业执行事件
     */
    public JobExecutionEvent executionFailure(final Throwable failureCause) {
        JobExecutionEvent result = new JobExecutionEvent(id, hostname, ip, taskId, jobName, source, shardingItem, startTime, completeTime, success, new JobExecutionEventThrowable(failureCause));
        result.setCompleteTime(new Date());
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 获取失败原因.
     * 
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
