/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.cloud.context;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 任务运行时上下文.
 *
 * @author zhangliang
 */
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public final class TaskContext {
    
    private static final String DELIMITER = "@-@";
    
    private final String id;
    
    private final String jobName;
    
    private final int shardingItem;
    
    private final ExecutionType type;
    
    public TaskContext(final String jobName, final int shardingItem, final ExecutionType type) {
        id = Joiner.on(DELIMITER).join(jobName, shardingItem, type, UUID.randomUUID().toString());
        this.jobName = jobName;
        this.shardingItem = shardingItem;
        this.type = type;
    }
    
    private TaskContext(final String id, final String jobName, final int shardingItem, final ExecutionType type) {
        this.id = id;
        this.jobName = jobName;
        this.shardingItem = shardingItem;
        this.type = type;
    }
    
    /**
     * 根据任务主键获取任务上下文.
     *
     * @param id 任务主键
     * @return 任务上下文
     */
    public static TaskContext from(final String id) {
        String[] result = id.split(DELIMITER);
        Preconditions.checkState(4 == result.length);
        return new TaskContext(id, result[0], Integer.parseInt(result[1]), ExecutionType.valueOf(result[2]));
    }
}
