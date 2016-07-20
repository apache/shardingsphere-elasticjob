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

package com.dangdang.ddframe.job.cloud.context;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 任务运行时上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
public final class TaskContext {
    
    private static final String DELIMITER = "@-@";
    
    private final String id;
    
    private final MetaInfo metaInfo;
    
    private final ExecutionType type;
    
    private final String slaveId;
    
    public TaskContext(final String jobName, final int shardingItem, final ExecutionType type, final String slaveId) {
        metaInfo = new MetaInfo(jobName, shardingItem);
        this.type = type;
        this.slaveId = slaveId;
        id = Joiner.on(DELIMITER).join(metaInfo, type, slaveId, UUID.randomUUID().toString());
    }
    
    /**
     * 根据任务主键获取任务上下文.
     *
     * @param id 任务主键
     * @return 任务上下文
     */
    public static TaskContext from(final String id) {
        String[] result = id.split(DELIMITER);
        Preconditions.checkState(5 == result.length);
        return new TaskContext(id, new MetaInfo(result[0], Integer.parseInt(result[1])), ExecutionType.valueOf(result[2]), result[3]);
    }
    
    /**
     * 任务元信息.
     */
    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class MetaInfo {
        
        private final String jobName;
        
        private final int shardingItem;
        
        /**
         * 根据任务元信息字符串获取元信息对象.
         *
         * @param value 任务元信息字符串
         * @return 元信息对象
         */
        public static MetaInfo from(final String value) {
            String[] result = value.split(DELIMITER);
            Preconditions.checkState(2 == result.length || 5 == result.length);
            return new MetaInfo(result[0], Integer.parseInt(result[1]));
        }
        
        @Override
        public String toString() {
            return Joiner.on(DELIMITER).join(jobName, shardingItem);
        }
    }
}
