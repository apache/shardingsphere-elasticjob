package com.dangdang.ddframe.job.event.type;

import com.dangdang.ddframe.job.context.ExecutionType;
import com.dangdang.ddframe.job.event.JobEvent;
import com.dangdang.ddframe.job.util.env.LocalHostService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * 作业状态痕迹事件.
 *
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
public class JobStatusTraceEvent implements JobEvent {
    
    private static LocalHostService localHostService = new LocalHostService();
    
    private final String id = UUID.randomUUID().toString();
    
    private final String hostname = localHostService.getHostName();
    
    private final String ip = localHostService.getIp();
    
    private final String jobName;
    
    private final String taskId;
    
    private final String slaveId;
    
    private final ExecutionType executionType;
    
    private final String shardingItems;
    
    private final Source source;
    
    private final State state;
    
    private final String message;
    
    private final Date creationTime = new Date();
    
    public enum State {
        TASK_STAGING, TASK_RUNNING, TASK_FINISHED, TASK_KILLED, TASK_LOST, TASK_FAILED, TASK_ERROR
    }
    
    public enum Source {
        CLOUD_SCHEDULER, CLOUD_EXECUTOR, LITE_EXECUTOR
    }
}
