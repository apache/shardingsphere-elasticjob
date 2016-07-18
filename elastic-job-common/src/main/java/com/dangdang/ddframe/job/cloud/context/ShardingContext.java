package com.dangdang.ddframe.job.cloud.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务运行时分片上下文.
 *
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingContext {
    
    private final int shardingItem;
    
    @Setter
    private boolean streamingProcess;
}
