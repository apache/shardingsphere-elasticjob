package com.dangdang.ddframe.job.lite.internal.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 作业运行实例信息.
 *
 * @author zhangliang
 */
@NoArgsConstructor
@Getter
@Setter
public final class InstanceInfo {
    
    private ServerStatus serverStatus = ServerStatus.READY;
    
    private boolean shutdown;
    
    public InstanceInfo(final ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }
}
