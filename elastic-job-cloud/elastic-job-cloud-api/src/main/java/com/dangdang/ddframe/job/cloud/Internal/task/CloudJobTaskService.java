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

package com.dangdang.ddframe.job.cloud.Internal.task;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.util.UUID;

/**
 * 云作业任务服务.
 *
 * @author zhangliang
 */
public class CloudJobTaskService {
    
    /**
     * 创建任务主键.
     *
     * @param jobName 作业名称
     * @param shardingItem 分片项
     * @return 任务主键
     */
    public String generateTaskId(final String jobName, final int shardingItem) {
        return Joiner.on("@-@").join(jobName, shardingItem, UUID.randomUUID().toString());
    }
    
    /**
     * 根据任务主键获取任务对象.
     *
     * @param taskId 任务主键
     * @return 任务对象
     */
    public CloudJobTask getJobTask(final String taskId) {
        String[] result = taskId.split("@-@");
        Preconditions.checkState(3 == result.length);
        return new CloudJobTask(result[2], result[0], Integer.parseInt(result[1]));
    }
}
