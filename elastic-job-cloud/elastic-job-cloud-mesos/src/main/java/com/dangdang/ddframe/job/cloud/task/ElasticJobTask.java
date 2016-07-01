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

package com.dangdang.ddframe.job.cloud.task;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.UUID;

/**
 * 云作业任务.
 *
 * @author zhangliang
 */
@Getter
public final class ElasticJobTask {
    
    private static final String DELIMITER = "@-@";
    
    private final String id;
    
    private final String jobName;
    
    private final int shardingItem;
    
    public ElasticJobTask(final String jobName, final int shardingItem) {
        id = Joiner.on(DELIMITER).join(jobName, shardingItem, UUID.randomUUID().toString());
        this.jobName = jobName;
        this.shardingItem = shardingItem;
    }
    
    private ElasticJobTask(final String id, final String jobName, final int shardingItem) {
        this.id = id;
        this.jobName = jobName;
        this.shardingItem = shardingItem;
    }
    
    /**
     * 根据任务主键获取任务对象.
     *
     * @param id 任务主键
     * @return 任务对象
     */
    public static ElasticJobTask from(final String id) {
        String[] result = id.split(DELIMITER);
        Preconditions.checkState(3 == result.length);
        return new ElasticJobTask(id, result[0], Integer.parseInt(result[1]));
    }
}
